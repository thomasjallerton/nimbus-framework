package com.nimbusframework.nimbuslocal

import com.nimbusframework.nimbuscore.services.ReadUserConfigService
import com.nimbusframework.nimbuscore.annotations.NimbusConstants
import com.nimbusframework.nimbuscore.clients.ClientBinder
import com.nimbusframework.nimbuscore.clients.file.FileStorageBucketNameAnnotationService
import com.nimbusframework.nimbuscore.clients.notification.NotificationTopicAnnotationService
import com.nimbusframework.nimbuscore.clients.queue.QueueIdAnnotationService
import com.nimbusframework.nimbuscore.eventabstractions.RequestContext
import com.nimbusframework.nimbuscore.persisted.FileUploadDescription
import com.nimbusframework.nimbuslocal.clients.LocalInternalClientBuilder
import com.nimbusframework.nimbuslocal.deployment.basic.BasicFunction
import com.nimbusframework.nimbuslocal.deployment.document.LocalDocumentStore
import com.nimbusframework.nimbuslocal.deployment.file.LocalFileStorage
import com.nimbusframework.nimbuslocal.deployment.function.FunctionEnvironment
import com.nimbusframework.nimbuslocal.deployment.function.FunctionIdentifier
import com.nimbusframework.nimbuslocal.deployment.http.HttpRequest
import com.nimbusframework.nimbuslocal.deployment.keyvalue.LocalKeyValueStore
import com.nimbusframework.nimbuslocal.deployment.notification.LocalNotificationTopic
import com.nimbusframework.nimbuslocal.deployment.queue.LocalQueue
import com.nimbusframework.nimbuslocal.deployment.services.FileService
import com.nimbusframework.nimbuslocal.deployment.services.LocalResourceHolder
import com.nimbusframework.nimbuslocal.deployment.services.StageService
import com.nimbusframework.nimbuslocal.deployment.services.function.*
import com.nimbusframework.nimbuslocal.deployment.services.resource.*
import com.nimbusframework.nimbuslocal.deployment.services.usesresources.*
import com.nimbusframework.nimbuslocal.deployment.webserver.WebServerHandler
import com.nimbusframework.nimbuslocal.deployment.websocket.WebSocketRequest
import org.eclipse.jetty.websocket.api.Session
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import org.reflections.util.FilterBuilder
import java.lang.reflect.InvocationTargetException

class LocalNimbusDeployment {

    private val httpPort: Int
    private val webSocketPort: Int

    internal val localResourceHolder: LocalResourceHolder

    private val localFunctionHandlers: MutableList<LocalFunctionHandler> = mutableListOf()
    private val localCreateResourcesHandlers: MutableList<LocalCreateResourcesHandler> = mutableListOf()
    private val localUseResourceHandlers: MutableList<LocalUsesResourcesHandler> = mutableListOf()

    private val variableSubstitution: MutableMap<String, String> = mutableMapOf()
    private val fileUploadDetails: MutableMap<Class<*>, MutableList<FileUploadDescription>> = mutableMapOf()

    private val userConfig = ReadUserConfigService().readUserConfig()

    private val serviceScanner: Reflections = Reflections(ConfigurationBuilder()
            .setScanners(SubTypesScanner(false))
            .addUrls(ClasspathHelper.forJavaClassPath())
            .filterInputsBy(
                    FilterBuilder()
                            .includePackage("com.nimbusframework")
                            .excludePackage("com.nimbusframework.nimbuslocal")
            ))


    private fun initialiseFunctionHandlers(stageService: StageService) {
        localFunctionHandlers.add(LocalAfterDeploymentHandler(localResourceHolder, stageService))
        localFunctionHandlers.add(LocalBasicFunctionHandler(localResourceHolder, stageService))
        localFunctionHandlers.add(LocalDocumentStoreFunctionHandler(localResourceHolder, stageService))
        localFunctionHandlers.add(LocalFileStorageFunctionHandler(localResourceHolder, stageService))
        localFunctionHandlers.add(LocalHttpFunctionHandler(localResourceHolder, httpPort, variableSubstitution, stageService))
        localFunctionHandlers.add(LocalKeyValueStoreFunctionHandler(localResourceHolder, stageService))
        localFunctionHandlers.add(LocalNotificationFunctionHandler(localResourceHolder, stageService))
        localFunctionHandlers.add(LocalQueueFunctionHandler(localResourceHolder, stageService))
        localFunctionHandlers.add(LocalWebSocketFunctionHandler(localResourceHolder, webSocketPort, variableSubstitution, stageService))
    }

    private fun initialiseResourceCreators(stageService: StageService) {
        localCreateResourcesHandlers.add(LocalDocumentStoreCreator(localResourceHolder, stageService))
        localCreateResourcesHandlers.add(LocalFileStorageCreator(localResourceHolder, httpPort, variableSubstitution, fileUploadDetails, stageService))
        localCreateResourcesHandlers.add(LocalKeyValueStoreCreator(localResourceHolder, stageService))
        localCreateResourcesHandlers.add(LocalNotificationTopicCreator(localResourceHolder, stageService))
        localCreateResourcesHandlers.add(LocalQueueCreator(localResourceHolder, stageService))

        val classes = serviceScanner.getSubTypesOf(LocalCreateResourcesHandler::class.java)
        val handlers = classes.map { clazz ->
            val constructor = clazz.getConstructor(LocalResourceHolder::class.java, StageService::class.java)
            constructor.newInstance(localResourceHolder, stageService)
        }
        localCreateResourcesHandlers.addAll(handlers)
    }

    private fun initialiseUseResourceHandlers(stageService: StageService) {
        localUseResourceHandlers.add(LocalUsesBasicFunctionHandler(localResourceHolder, stageService))
        localUseResourceHandlers.add(LocalUsesDocumentStoreHandler(localResourceHolder, stageService))
        localUseResourceHandlers.add(LocalUsesFileStorageClientHandler(localResourceHolder, stageService))
        localUseResourceHandlers.add(LocalUsesKeyValueStoreHandler(localResourceHolder, stageService))
        localUseResourceHandlers.add(LocalUsesNotificationTopicHandler(localResourceHolder, stageService))
        localUseResourceHandlers.add(LocalUsesQueueHandler(localResourceHolder, stageService))
        localUseResourceHandlers.add(LocalUsesRelationalDatabaseHandler(localResourceHolder, stageService))
        localUseResourceHandlers.add(LocalEnvironmentVariableHandler(localResourceHolder, stageService))
        localUseResourceHandlers.add(LocalUsesWebSocketHandler(localResourceHolder, stageService))
    }

    private constructor(stageParam: String = NimbusConstants.stage, httpPort: Int = 8080, webSocketPort: Int = 8081, classes: Array<out Class<out Any>>) {
        instance = this
        stage = stageParam
        localResourceHolder = LocalResourceHolder(stage)
        this.httpPort = httpPort
        this.webSocketPort = webSocketPort

        val stageService = StageService(stageParam, userConfig.defaultStages.contains(stage))

        initialiseFunctionHandlers(stageService)
        initialiseResourceCreators(stageService)
        initialiseUseResourceHandlers(stageService)

        val allClasses = mutableListOf<Class<*>>()
        allClasses.addAll(classes)

        for (clazz in classes) {
            allClasses.addAll(getNestedClasses(clazz))
        }

        //Handle Resources that need to exist for handlers to work
        allClasses.forEach { clazz -> createResources(clazz) }

        //Handle function handlers
        allClasses.forEach { clazz -> createHandlers(clazz) }

        //Handle use resources
        allClasses.forEach { clazz -> handleUseResources(clazz) }

        val fileService = FileService(variableSubstitution)

        fileService.handleUploadingFile(fileUploadDetails)

        localResourceHolder.afterDeployments.forEach { method -> method.invoke() }
    }

    private fun getNestedClasses(clazz: Class<out Any>): List<Class<*>> {
        val result = mutableListOf<Class<*>>()
        result.addAll(clazz.classes)
        clazz.classes.forEach { result.addAll(getNestedClasses(it)) }

        return result
    }


    private constructor(packageName: String, stageParam: String = NimbusConstants.stage, httpPort: Int = 8080, webSocketPort: Int = 8081) {
        instance = this
        stage = stageParam
        localResourceHolder = LocalResourceHolder(stage)

        this.httpPort = httpPort
        this.webSocketPort = webSocketPort

        val stageService = StageService(stageParam, userConfig.defaultStages.contains(stage))

        initialiseFunctionHandlers(stageService)
        initialiseResourceCreators(stageService)
        initialiseUseResourceHandlers(stageService)

        val reflections = Reflections(ConfigurationBuilder()
                .setScanners(SubTypesScanner(false))
                .addUrls(ClasspathHelper.forJavaClassPath())
                .filterInputsBy(FilterBuilder().include(FilterBuilder.prefix(packageName))))

        val allClasses = reflections.getSubTypesOf(Any::class.java).distinct().filter {clazz ->
            try {
                clazz.declaredMethods
                true
            } catch (e: NoClassDefFoundError) {
                false
            }
        }

        //Handle Resources that need to exist for handlers to work
        allClasses.forEach { clazz -> createResources(clazz) }

        //Handle function handlers
        allClasses.forEach { clazz -> createHandlers(clazz) }

        //Handle use resources
        allClasses.forEach { clazz -> handleUseResources(clazz) }

        val fileService = FileService(variableSubstitution)

        fileService.handleUploadingFile(fileUploadDetails)

        localResourceHolder.afterDeployments.forEach { method -> method.invoke() }
    }


    private fun createResources(clazz: Class<out Any>) {
        localCreateResourcesHandlers.forEach { handler -> handler.createResource(clazz) }
    }

    private fun createHandlers(clazz: Class<out Any>) {
        try {
            for (method in clazz.declaredMethods) {
                localFunctionHandlers.forEach { functionHandler -> functionHandler.createLocalFunctions(clazz, method) }
            }
        } catch (e: InvocationTargetException) {
            System.err.println("Error creating handler class ${clazz.canonicalName}, it should have no constructor parameters")
            e.targetException.printStackTrace()
        }
    }

    private fun handleUseResources(clazz: Class<out Any>) {
        for (method in clazz.methods) {
            localUseResourceHandlers.forEach { handler -> handler.handleFunctionEnvironment(clazz, method) }
        }
    }


    internal fun getLocalHandler(bucketName: String): WebServerHandler? {
        return localResourceHolder.httpServers[bucketName]
    }

    internal fun getWebSocketSessions(): Map<String, Session> {
        return localResourceHolder.webSocketSessions
    }

    internal fun getFunctionEnvironments(): Map<FunctionIdentifier, FunctionEnvironment> {
        return localResourceHolder.functionEnvironments
    }

    //--------------------------------------------User Facing Methods---------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------

    private fun startWebSocketServer(async: Boolean) {
        if (localResourceHolder.webSocketServer.canRun()) {
            println("WebSocket Server at: wss://localhost:$webSocketPort")
            localResourceHolder.webSocketServer.setup(webSocketPort)
            if (async) {
                localResourceHolder.webSocketServer.startWithoutJoin()
            } else {
                localResourceHolder.webSocketServer.start()
            }
        }
    }

    fun startWebSocketServer() {
        startWebSocketServer(false)
    }

    fun startAllServers() {
        val httpAsync = localResourceHolder.webSocketServer.canRun()
        startAllHttpServers(httpAsync)
        startWebSocketServer(false)
    }

    fun startAllServersAsync() {
        startAllHttpServers(true)
        startWebSocketServer(true)
    }

    fun stopAllServers() {
        localResourceHolder.webSocketServer.stop()
        localResourceHolder.httpServer.stopServer()
    }

    fun startFileBucketHttpServer(bucketName: String) {
        val localHttpServers = localResourceHolder.httpServers
        if (localHttpServers.containsKey(bucketName)) {
            println("HTTP Server at: http://localhost:$httpPort/$bucketName")
            val handler = localHttpServers[bucketName]!!
            val httpServer = localResourceHolder.httpServer
            httpServer.handler.addResource(bucketName, handler)
            httpServer.startServer(httpPort)
        } else {
            throw ResourceNotFoundException()
        }
    }

    fun startServerlessFunctionWebserver() {
        val localHttpServers = localResourceHolder.httpServers
        if (localHttpServers.containsKey(functionWebserverIdentifier)) {
            println("HTTP Server at: http://localhost:$httpPort/$functionWebserverIdentifier")
            val handler = localHttpServers[functionWebserverIdentifier]!!
            val httpServer = localResourceHolder.httpServer
            httpServer.handler.addResource(functionWebserverIdentifier, handler)
            httpServer.startServer(httpPort)
        } else {
            throw ResourceNotFoundException()
        }
    }

    fun startAllHttpServers() {
        startAllHttpServers(false)
    }

    private fun startAllHttpServers(async: Boolean) {
        val allResourcesHttpServer = localResourceHolder.httpServer
        for ((identifier, handler) in localResourceHolder.httpServers) {
            println("HTTP Server at: http://localhost:$httpPort/$identifier")
            allResourcesHttpServer.handler.addResource(identifier, handler)
        }
        if (async) {
            allResourcesHttpServer.startServerWithoutJoin(httpPort)
        } else {
            allResourcesHttpServer.startServer(httpPort)
        }
    }


    fun <K, V> getKeyValueStore(valueClass: Class<V>): LocalKeyValueStore<K, V> {
        val keyValueStores = localResourceHolder.keyValueStores

        if (keyValueStores.containsKey(valueClass)) {
            return keyValueStores[valueClass] as LocalKeyValueStore<K, V>
        } else {
            throw ResourceNotFoundException()
        }
    }

    fun getLocalFileStorage(bucketName: String): LocalFileStorage {
        val fileStorage = localResourceHolder.fileStorage

        if (fileStorage.containsKey(bucketName)) {
            return fileStorage[bucketName]!!
        } else {
            throw ResourceNotFoundException()
        }
    }

    fun getLocalFileStorage(bucketClass: Class<*>): LocalFileStorage {
        val bucketName = FileStorageBucketNameAnnotationService.getBucketName(bucketClass, stage)
        return getLocalFileStorage(bucketName)
    }

    fun <T> getDocumentStore(clazz: Class<T>): LocalDocumentStore<T> {
        val documentStores = localResourceHolder.documentStores

        if (documentStores.containsKey(clazz)) {
            return documentStores[clazz] as LocalDocumentStore<T>
        } else {
            throw ResourceNotFoundException()
        }
    }

    fun <T> getBasicFunction(clazz:Class<T>, methodName: String): BasicFunction {
        val functionIdentifier = FunctionIdentifier(clazz.canonicalName, methodName)
        val localBasicMethods = localResourceHolder.basicMethods

        if (localBasicMethods.containsKey(functionIdentifier)) {
            return localBasicMethods[functionIdentifier]!!
        } else {
            throw ResourceNotFoundException()
        }
    }

    fun getQueue(queueClass: Class<*>): LocalQueue {
        val queueId = QueueIdAnnotationService.getQueueId(queueClass, stage)
        val localQueues = localResourceHolder.queues

        if (localQueues.containsKey(queueId)) {
            return localQueues[queueId]!!
        } else {
            throw ResourceNotFoundException()
        }
    }

    fun getNotificationTopic(notificationTopicClass: Class<*>): LocalNotificationTopic {
        val topicName = NotificationTopicAnnotationService.getTopicName(notificationTopicClass, stage)
        val notificationTopics = localResourceHolder.notificationTopics

        if (notificationTopics.containsKey(topicName)) {
            return notificationTopics[topicName]!!
        } else {
            throw ResourceNotFoundException()
        }
    }

    fun <T> getMethod(clazz: Class<T>, methodName: String): ServerlessMethod {
        val functionIdentifier = FunctionIdentifier(clazz.canonicalName, methodName)
        val methods = localResourceHolder.functions

        if (methods.containsKey(functionIdentifier)) {
            return methods[functionIdentifier]!!.serverlessMethod
        } else {
            throw ResourceNotFoundException()
        }
    }

    fun sendHttpRequest(request: HttpRequest): Any? {
        val localHttpMethods = localResourceHolder.httpMethods
        val mainPath = request.path.split("?")[0]
        for ((identifier, method) in localHttpMethods) {
            if (identifier.matches(mainPath, request.method)) {
                return method.invoke(request, identifier)
            }
        }
        throw ResourceNotFoundException()
    }

    fun connectToWebSockets(headers: Map<String, String> = mapOf(), queryStringParams: Map<String, String> = mapOf()) {
        val topic = "\$connect"
        val request = WebSocketRequest("{\"topic\":\"\$connect\"}", queryStringParams, headers)
        request.requestContext = RequestContext("MESSAGE", "testConnection")
        val localWebsocketMethods = localResourceHolder.websocketMethods

        if (localWebsocketMethods.containsKey(topic)) {
            localWebsocketMethods[topic]!!.invoke(request)
        } else {
            throw ResourceNotFoundException()
        }
    }

    fun disconnectFromWebSockets() {
        val topic = "\$disconnect"
        val request = WebSocketRequest("{\"topic\":\"\$disconnect\"}")
        request.requestContext = RequestContext("MESSAGE", "testConnection")
        val localWebsocketMethods = localResourceHolder.websocketMethods

        if (localWebsocketMethods.containsKey(topic)) {
            localWebsocketMethods[topic]!!.invoke(request)
        } else {
            throw ResourceNotFoundException()
        }
    }

    fun sendWebSocketRequest(request: WebSocketRequest) {
        val topic = request.getTopic()
        request.requestContext = RequestContext("MESSAGE", "testConnection")
        val localWebsocketMethods = localResourceHolder.websocketMethods

        when {
            localWebsocketMethods.containsKey(topic) -> localWebsocketMethods[topic]!!.invoke(request)
            localWebsocketMethods.containsKey("\$default") -> localWebsocketMethods["\$default"]!!.invoke(request)
            else -> throw ResourceNotFoundException()
        }
    }

    companion object {
        private lateinit var instance: LocalNimbusDeployment
        internal lateinit var stage: String

        @JvmStatic
        fun getInstance(): LocalNimbusDeployment {
            ClientBinder.setInternalBuilder(LocalInternalClientBuilder)
            return instance
        }

        @JvmStatic
        fun getNewInstance(packageName: String): LocalNimbusDeployment {
            ClientBinder.setInternalBuilder(LocalInternalClientBuilder)
            LocalNimbusDeployment(packageName)
            return instance
        }

        @JvmStatic
        fun getNewInstance(packageName: String, stage: String, httpPort: Int, webSocketPort: Int): LocalNimbusDeployment {
            ClientBinder.setInternalBuilder(LocalInternalClientBuilder)
            LocalNimbusDeployment(packageName, stage, httpPort, webSocketPort)
            return instance
        }

        @JvmStatic
        @SafeVarargs
        fun getNewInstance(vararg clazz: Class<out Any>): LocalNimbusDeployment {
            ClientBinder.setInternalBuilder(LocalInternalClientBuilder)
            LocalNimbusDeployment(classes = clazz)
            return instance
        }

        @JvmStatic
        @SafeVarargs
        fun getNewInstance(stage: String, port: Int, webSocketPort: Int, vararg clazz: Class<out Any>): LocalNimbusDeployment {
            ClientBinder.setInternalBuilder(LocalInternalClientBuilder)
            LocalNimbusDeployment(stage, port, webSocketPort, clazz)
            return instance
        }

        const val functionWebserverIdentifier = "function"
    }
}
