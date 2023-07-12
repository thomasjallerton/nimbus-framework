package com.nimbusframework.nimbuslocal

import com.nimbusframework.nimbuscore.services.ReadUserConfigService
import com.nimbusframework.nimbuscore.annotations.NimbusConstants
import com.nimbusframework.nimbuscore.annotations.http.HttpException
import com.nimbusframework.nimbuscore.clients.ClientBinder
import com.nimbusframework.nimbuscore.clients.file.FileStorageBucketNameAnnotationService
import com.nimbusframework.nimbuscore.clients.notification.NotificationTopicAnnotationService
import com.nimbusframework.nimbuscore.clients.queue.QueueIdAnnotationService
import com.nimbusframework.nimbuscore.eventabstractions.RequestContext
import com.nimbusframework.nimbuscore.persisted.FileUploadDescription
import com.nimbusframework.nimbuslocal.clients.LocalInternalClientBuilder
import com.nimbusframework.nimbuslocal.deployment.CloudSpecificLocalDeployment
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
import com.nimbusframework.nimbuslocal.deployment.webserver.InternalPortCount
import com.nimbusframework.nimbuslocal.deployment.webserver.WebServerHandler
import com.nimbusframework.nimbuslocal.deployment.websocket.WebSocketRequest
import org.eclipse.jetty.websocket.api.Session
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import org.reflections.util.FilterBuilder
import java.lang.reflect.InvocationTargetException

class LocalNimbusDeployment private constructor(
    private val stage: String,
    private val httpPort: Int = 8080,
    private val webSocketPort: Int = 8079,
    private val fileStorageBucketPorts: Map<Class<*>, Int>,
    classes: Collection<Class<*>>,
    private val specificLocalDeployment: CloudSpecificLocalDeployment? = null
) {

    internal val localResourceHolder: LocalResourceHolder

    private val localFunctionHandlers: MutableList<LocalFunctionHandler> = mutableListOf()
    private val localCreateResourcesHandlers: MutableList<LocalCreateResourcesHandler> = mutableListOf()
    private val localUseResourceHandlers: MutableList<LocalUsesResourcesHandler> = mutableListOf()

    private val variableSubstitution: MutableMap<String, String> = mutableMapOf()
    private val fileUploadDetails: MutableMap<Class<*>, MutableList<FileUploadDescription>> = mutableMapOf()

    private val userConfig = ReadUserConfigService().readUserConfig()

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

        if (specificLocalDeployment != null) {
            localFunctionHandlers.addAll(specificLocalDeployment.getLocalFunctionHandlers(localResourceHolder, stageService))
        }
    }

    private fun initialiseResourceCreators(stageService: StageService) {
        localCreateResourcesHandlers.add(LocalDocumentStoreCreator(localResourceHolder, stageService))
        localCreateResourcesHandlers.add(LocalFileStorageCreator(localResourceHolder, httpPort, variableSubstitution, fileUploadDetails, fileStorageBucketPorts, stageService))
        localCreateResourcesHandlers.add(LocalKeyValueStoreCreator(localResourceHolder, stageService))
        localCreateResourcesHandlers.add(LocalNotificationTopicCreator(localResourceHolder, stageService))
        localCreateResourcesHandlers.add(LocalQueueCreator(localResourceHolder, stageService))

        if (specificLocalDeployment != null) {
            localCreateResourcesHandlers.addAll(specificLocalDeployment.getLocalCreateResourcesHandlers(localResourceHolder, stageService))
        }
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

    init {
        ClientBinder.setInternalBuilder(LocalInternalClientBuilder)

        instance = this
        Companion.stage = stage
        InternalPortCount.currentPort = httpPort + 1
        localResourceHolder = LocalResourceHolder()
        val stageService = StageService(stage, userConfig.defaultStages.contains(stage), userConfig.getAllowedOrigins(), userConfig.getAllowedHeaders())
        initialiseFunctionHandlers(stageService)
        initialiseResourceCreators(stageService)
        initialiseUseResourceHandlers(stageService)
        classes.forEach { clazz -> createResources(clazz) }
        classes.forEach { clazz -> createHandlers(clazz) }
        classes.forEach { clazz -> handleUseResources(clazz) }
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
        return localResourceHolder.httpServers[bucketName]?.webServerHandler
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
        localResourceHolder.httpServers.forEach { it.value.stopServer() }
    }

    fun startFileBucketHttpServer(bucketName: String) {
        val localHttpServers = localResourceHolder.httpServers
        if (localHttpServers.containsKey(bucketName)) {
            val httpServer = localHttpServers[bucketName]!!
            println("HTTP Server at: http://localhost:${httpServer.port}")
            httpServer.startServer()
        } else {
            throw ResourceNotFoundException()
        }
    }

    fun startServerlessFunctionWebserver() {
        val localHttpServers = localResourceHolder.httpServers
        if (localHttpServers.containsKey(functionWebserverIdentifier)) {
            val httpServer = localHttpServers[functionWebserverIdentifier]!!
            println("HTTP Server at: http://localhost:${httpServer.port}")
            httpServer.startServer()
        } else {
            throw ResourceNotFoundException()
        }
    }

    fun startAllHttpServers() {
        startAllHttpServers(false)
    }

    private fun startAllHttpServers(async: Boolean) {
        val allServers = localResourceHolder.httpServers.entries.toList()
        val asyncServers = allServers.drop(1)
        val (finalIdentifier, finalServer) = allServers.first()
        for ((identifier, server) in asyncServers) {
            println("HTTP Server for $identifier at: http://localhost:${server.port}/")
            server.startServerWithoutJoin()
        }
        println("HTTP Server for $finalIdentifier at: http://localhost:${finalServer.port}/")
        if (async) {
            finalServer.startServerWithoutJoin()
        } else {
            finalServer.startServer()
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
                val authenticationResponse = localResourceHolder.httpAuthenticator?.allow(request)
                if (authenticationResponse?.authenticated == false) {
                    throw HttpException(403, "Unauthorized")
                }
                return method.invoke(request, identifier, authenticationResponse?.context ?: mapOf())
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

    class Builder {
        private var stage: String = NimbusConstants.stage
        private var classes: List<Class<*>> = listOf()
        private var httpApiPort: Int = 8080
        private var webSocketApiPort: Int = 8081
        private val fileStorageBucketPorts: MutableMap<Class<*>, Int> = mutableMapOf()
        private var specificLocalDeployment: CloudSpecificLocalDeployment? = null

        fun withStage(stage: String): Builder {
            this.stage = stage
            return this
        }

        fun withHttpApiPort(port: Int): Builder {
            httpApiPort = port
            return this
        }

        fun withWebSocketApiPort(port: Int): Builder {
            webSocketApiPort = port
            return this
        }

        fun withFileStorageBucketPort(bucketClass: Class<*>, port: Int): Builder {
            fileStorageBucketPorts[bucketClass] = port
            return this
        }

        fun withClasses(vararg classes: Class<*>): Builder {
            if (this.classes.isNotEmpty()) {
                throw IllegalArgumentException("Classes has already been set")
            }
            this.classes = classes.toList()
            return this
        }

        fun withClasses(classes: Collection<Class<*>>): Builder {
            if (this.classes.isNotEmpty()) {
                throw IllegalArgumentException("Classes has already been set")
            }
            this.classes = classes.toList()
            return this
        }

        fun withSpecificLocalDeployment(specificLocalDeployment: CloudSpecificLocalDeployment): Builder {
            this.specificLocalDeployment = specificLocalDeployment
            return this
        }

        fun withClassesInPackage(packageName: String): Builder {
            if (this.classes.isNotEmpty()) {
                throw IllegalArgumentException("Classes has already been set")
            }
            val reflections = Reflections(ConfigurationBuilder()
                .setScanners(Scanners.SubTypes.filterResultsBy { p -> true })
                .addUrls(ClasspathHelper.forJavaClassPath())
                .filterInputsBy(FilterBuilder().includePackage(packageName)))

            this.classes = reflections.getSubTypesOf(Any::class.java).distinct().filter {clazz ->
                try {
                    clazz.declaredMethods
                    true
                } catch (e: NoClassDefFoundError) {
                    false
                }
            }
            return this
        }

        fun build(): LocalNimbusDeployment {
            return LocalNimbusDeployment(
                stage, httpApiPort, webSocketApiPort, fileStorageBucketPorts, classes, specificLocalDeployment
            )
        }
    }

    companion object {
        private lateinit var instance: LocalNimbusDeployment
        lateinit var stage: String

        @JvmStatic
        fun getInstance(): LocalNimbusDeployment {
            return instance
        }

        @JvmStatic
        fun getNewInstance(packageName: String): LocalNimbusDeployment {
            Builder().withClassesInPackage(packageName).build()
            return instance
        }

        @JvmStatic
        fun getNewInstance(packageName: String, specificLocalDeployment: CloudSpecificLocalDeployment): LocalNimbusDeployment {
            Builder()
                .withClassesInPackage(packageName)
                .withSpecificLocalDeployment(specificLocalDeployment)
                .build()
            return instance
        }

        @JvmStatic
        fun getNewInstance(packageName: String, stage: String, httpPort: Int, webSocketPort: Int): LocalNimbusDeployment {
            Builder()
                .withClassesInPackage(packageName)
                .withStage(stage)
                .withHttpApiPort(httpPort)
                .withWebSocketApiPort(webSocketPort)
                .build()
            return instance
        }

        @JvmStatic
        fun getNewInstance(packageName: String, stage: String, httpPort: Int, webSocketPort: Int, specificLocalDeployment: CloudSpecificLocalDeployment): LocalNimbusDeployment {
            Builder()
                .withClassesInPackage(packageName)
                .withStage(stage)
                .withHttpApiPort(httpPort)
                .withWebSocketApiPort(webSocketPort)
                .withSpecificLocalDeployment(specificLocalDeployment)
                .build()
            return instance
        }

        @JvmStatic
        @SafeVarargs
        fun getNewInstance(vararg clazz: Class<*>): LocalNimbusDeployment {
            Builder()
                .withClasses(clazz.toList())
                .build()
            return instance
        }

        @JvmStatic
        @SafeVarargs
        fun getNewInstance(vararg clazz: Class<*>, specificLocalDeployment: CloudSpecificLocalDeployment): LocalNimbusDeployment {
            Builder()
                .withClasses(clazz.toList())
                .withSpecificLocalDeployment(specificLocalDeployment)
                .build()
            return instance
        }

        @JvmStatic
        @SafeVarargs
        fun getNewInstance(stage: String, port: Int, webSocketPort: Int, vararg clazz: Class<out Any>): LocalNimbusDeployment {
            Builder()
                .withClasses(clazz.toList())
                .withStage(stage)
                .withHttpApiPort(port)
                .withWebSocketApiPort(webSocketPort)
                .build()
            return instance
        }

        @JvmStatic
        @SafeVarargs
        fun getNewInstance(stage: String, port: Int, webSocketPort: Int, vararg clazz: Class<out Any>, specificLocalDeployment: CloudSpecificLocalDeployment): LocalNimbusDeployment {
            Builder()
                .withClasses(clazz.toList())
                .withStage(stage)
                .withHttpApiPort(port)
                .withWebSocketApiPort(webSocketPort)
                .withSpecificLocalDeployment(specificLocalDeployment)
                .build()
            return instance
        }

        @JvmStatic
        @SafeVarargs
        fun getNewInstance(builder: Builder): LocalNimbusDeployment {
            builder.build()
            return instance
        }

        @JvmStatic
        @SafeVarargs
        fun getNewInstance(consumeBuilder: (Builder) -> Unit): LocalNimbusDeployment {
            val builder = Builder()
            consumeBuilder(builder)
            builder.build()
            return instance
        }

        const val functionWebserverIdentifier = "function"
    }
}
