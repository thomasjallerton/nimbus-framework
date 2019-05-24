package com.nimbusframework.nimbuscore.testing

import com.nimbusframework.nimbuscore.clients.ClientBuilder
import com.nimbusframework.nimbuscore.clients.document.AbstractDocumentStoreClient
import com.nimbusframework.nimbuscore.clients.keyvalue.AbstractKeyValueStoreClient
import com.nimbusframework.nimbuscore.persisted.FileUploadDescription
import com.nimbusframework.nimbuscore.testing.basic.BasicMethod
import com.nimbusframework.nimbuscore.testing.document.LocalDocumentStore
import com.nimbusframework.nimbuscore.testing.file.LocalFileStorage
import com.nimbusframework.nimbuscore.testing.function.FunctionEnvironment
import com.nimbusframework.nimbuscore.testing.function.FunctionIdentifier
import com.nimbusframework.nimbuscore.testing.http.HttpRequest
import com.nimbusframework.nimbuscore.testing.keyvalue.LocalKeyValueStore
import com.nimbusframework.nimbuscore.testing.notification.LocalNotificationTopic
import com.nimbusframework.nimbuscore.testing.queue.LocalQueue
import com.nimbusframework.nimbuscore.testing.services.FileService
import com.nimbusframework.nimbuscore.testing.services.LocalResourceHolder
import com.nimbusframework.nimbuscore.testing.services.function.*
import com.nimbusframework.nimbuscore.testing.services.resource.*
import com.nimbusframework.nimbuscore.testing.services.usesresources.*
import com.nimbusframework.nimbuscore.testing.webserver.WebserverHandler
import com.nimbusframework.nimbuscore.testing.websocket.WebSocketRequest
import com.nimbusframework.nimbuscore.wrappers.websocket.models.RequestContext
import org.eclipse.jetty.websocket.api.Session
import org.reflections.Reflections
import org.reflections.scanners.ResourcesScanner
import org.reflections.scanners.SubTypesScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import org.reflections.util.FilterBuilder
import java.lang.reflect.InvocationTargetException
import java.util.*


class LocalNimbusDeployment {

    private val httpPort: Int
    private val webSocketPort: Int

    private val localResourceHolder = LocalResourceHolder()

    private val localFunctionHandlers: MutableList<LocalFunctionHandler> = mutableListOf()
    private val localCreateResourcesHandlers: MutableList<LocalCreateResourcesHandler> = mutableListOf()
    private val localUseResourceHandlers: MutableList<LocalUsesResourcesHandler> = mutableListOf()

    private val variableSubstitution: MutableMap<String, String> = mutableMapOf()
    private val fileUploadDetails: MutableMap<String, MutableList<FileUploadDescription>> = mutableMapOf()

    private fun initialiseFunctionHandlers() {
        localFunctionHandlers.add(LocalAfterDeploymentHandler(localResourceHolder, stage))
        localFunctionHandlers.add(LocalBasicFunctionHandler(localResourceHolder, stage))
        localFunctionHandlers.add(LocalDocumentStoreFunctionHandler(localResourceHolder, stage))
        localFunctionHandlers.add(LocalFileStorageFunctionHandler(localResourceHolder, stage))
        localFunctionHandlers.add(LocalHttpFunctionHandler(localResourceHolder, httpPort, variableSubstitution, stage))
        localFunctionHandlers.add(LocalKeyValueStoreFunctionHandler(localResourceHolder, stage))
        localFunctionHandlers.add(LocalNotificationFunctionHandler(localResourceHolder, stage))
        localFunctionHandlers.add(LocalQueueFunctionHandler(localResourceHolder, stage))
        localFunctionHandlers.add(LocalWebSocketFunctionHandler(localResourceHolder, webSocketPort, variableSubstitution, stage))
    }

    private fun initialiseResourceCreators() {
        localCreateResourcesHandlers.add(LocalDocumentStoreCreator(localResourceHolder, stage))
        localCreateResourcesHandlers.add(LocalFileStorageCreator(localResourceHolder, httpPort, variableSubstitution, fileUploadDetails, stage))
        localCreateResourcesHandlers.add(LocalKeyValueStoreCreator(localResourceHolder, stage))
        localCreateResourcesHandlers.add(LocalNotificationTopicCreator(localResourceHolder, stage))
    }

    private fun initialiseUseResourceHandlers() {
        localUseResourceHandlers.add(LocalUsesBasicFunctionHandler(localResourceHolder, stage))
        localUseResourceHandlers.add(LocalUsesDocumentStoreHandler(localResourceHolder, stage))
        localUseResourceHandlers.add(LocalUsesFileStorageClientHandler(localResourceHolder, stage))
        localUseResourceHandlers.add(LocalUsesKeyValueStoreHandler(localResourceHolder, stage))
        localUseResourceHandlers.add(LocalUsesNotificationTopicHandler(localResourceHolder, stage))
        localUseResourceHandlers.add(LocalUsesQueueHandler(localResourceHolder, stage))
        localUseResourceHandlers.add(LocalUsesRelationalDatabaseHandler(localResourceHolder, stage))
        localUseResourceHandlers.add(LocalEnvironmentVariableHandler(localResourceHolder, stage))
        localUseResourceHandlers.add(LocalUsesWebSocketHandler(localResourceHolder, stage))
    }

    private constructor(clazz: Class<out Any>, stageParam: String = "dev", httpPort: Int = 8080, webSocketPort: Int = 8081) {
        instance = this
        stage = stageParam
        this.httpPort = httpPort
        this.webSocketPort = webSocketPort

        initialiseFunctionHandlers()
        initialiseResourceCreators()
        initialiseUseResourceHandlers()

        createResources(clazz)
        createHandlers(clazz)
        handleUseResources(clazz)

        val fileService = FileService(variableSubstitution)

        fileService.handleUploadingFile(fileUploadDetails)

        localResourceHolder.afterDeployments.forEach { method -> method.invoke() }
    }

    private constructor(packageName: String, stageParam: String = "dev", httpPort: Int = 8080, webSocketPort: Int = 8081) {
        instance = this
        stage = stageParam
        this.httpPort = httpPort
        this.webSocketPort = webSocketPort

        initialiseFunctionHandlers()
        initialiseResourceCreators()
        initialiseUseResourceHandlers()

        val classLoadersList = LinkedList<ClassLoader>()
        classLoadersList.add(ClasspathHelper.contextClassLoader())
        classLoadersList.add(ClasspathHelper.staticClassLoader())

        val reflections = Reflections(ConfigurationBuilder()
                .setScanners(SubTypesScanner(false), ResourcesScanner())
                .setUrls(ClasspathHelper.forClassLoader(*classLoadersList.toTypedArray()))
                .filterInputsBy(FilterBuilder().include(FilterBuilder.prefix(packageName))))

        val allClasses = reflections.getSubTypesOf(Any::class.java)

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


    internal fun getLocalHandler(bucketName: String): WebserverHandler? {
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

    fun startWebSocketServer() {
        localResourceHolder.webSocketServer.setup(webSocketPort)
        localResourceHolder.webSocketServer.start()
    }

    fun startAllServers() {
        localResourceHolder.webSocketServer.setup(webSocketPort)
        localResourceHolder.webSocketServer.startWithoutJoin()
        startAllHttpServers()
    }

    fun startAllServersAsync() {
        localResourceHolder.webSocketServer.setup(webSocketPort)
        localResourceHolder.webSocketServer.startWithoutJoin()
        startAllHttpServersAsync()
    }

    fun stopAllServers() {
        localResourceHolder.webSocketServer.stop()
        localResourceHolder.httpServer.stopServer()
    }

    fun startFileBucketHttpServer(bucketName: String) {
        val localHttpServers = localResourceHolder.httpServers
        if (localHttpServers.containsKey(bucketName)) {
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
            val handler = localHttpServers[functionWebserverIdentifier]!!
            val httpServer = localResourceHolder.httpServer
            httpServer.handler.addResource(functionWebserverIdentifier, handler)
            httpServer.startServer(httpPort)
        } else {
            throw ResourceNotFoundException()
        }
    }

    fun startAllHttpServers() {
        val allResourcesHttpServer = localResourceHolder.httpServer
        for ((identifier, handler) in localResourceHolder.httpServers) {
            allResourcesHttpServer.handler.addResource(identifier, handler)
        }
        allResourcesHttpServer.startServer(httpPort)
    }

    private fun startAllHttpServersAsync() {
        val allResourcesHttpServer = localResourceHolder.httpServer
        for ((identifier, handler) in localResourceHolder.httpServers) {
            allResourcesHttpServer.handler.addResource(identifier, handler)
        }
        allResourcesHttpServer.startServerWithoutJoin(httpPort)
    }


    fun <K, V> getKeyValueStore(valueClass: Class<V>): LocalKeyValueStore<K, V> {
        val tableName = AbstractKeyValueStoreClient.getTableName(valueClass, stage)
        val keyValueStores = localResourceHolder.keyValueStores

        if (keyValueStores.containsKey(tableName)) {
            return keyValueStores[tableName] as LocalKeyValueStore<K, V>
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

    fun <T> getDocumentStore(clazz: Class<T>): LocalDocumentStore<T> {
        val tableName = AbstractDocumentStoreClient.getTableName(clazz, stage)
        val documentStores = localResourceHolder.documentStores

        if (documentStores.containsKey(tableName)) {
            return documentStores[tableName] as LocalDocumentStore<T>
        } else {
            throw ResourceNotFoundException()
        }
    }

    internal fun <T> getBasicMethod(clazz:Class<T>, methodName: String): BasicMethod {
        val functionIdentifier = FunctionIdentifier(clazz.canonicalName, methodName)
        val localBasicMethods = localResourceHolder.basicMethods

        if (localBasicMethods.containsKey(functionIdentifier)) {
            return localBasicMethods[functionIdentifier]!!
        } else {
            throw ResourceNotFoundException()
        }
    }

    fun getQueue(id: String): LocalQueue {
        val localQueues = localResourceHolder.queues

        if (localQueues.containsKey(id)) {
            return localQueues[id]!!
        } else {
            throw ResourceNotFoundException()
        }
    }

    fun getNotificationTopic(topic: String): LocalNotificationTopic {
        val notificationTopics = localResourceHolder.notificationTopics

        if (notificationTopics.containsKey(topic)) {
            return notificationTopics[topic]!!
        } else {
            throw ResourceNotFoundException()
        }
    }

    fun <T> getMethod(clazz: Class<T>, methodName: String): ServerlessMethod {
        val functionIdentifier = FunctionIdentifier(clazz.canonicalName, methodName)
        val methods = localResourceHolder.methods

        if (methods.containsKey(functionIdentifier)) {
            return methods[functionIdentifier]!!
        } else {
            throw ResourceNotFoundException()
        }
    }

    fun sendHttpRequest(request: HttpRequest) {
        val localHttpMethods = localResourceHolder.httpMethods

        for ((identifier, method) in localHttpMethods) {
            if (identifier.matches(request.path, request.method)) {
                method.invoke(request, identifier)
                return
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
            return instance
        }

        @JvmStatic
        fun getNewInstance(packageName: String): LocalNimbusDeployment {
            ClientBuilder.isLocalDeployment = true
            LocalNimbusDeployment(packageName)
            return instance
        }

        @JvmStatic
        fun getNewInstance(packageName: String, stage: String, httpPort: Int, webSocketPort: Int): LocalNimbusDeployment {
            ClientBuilder.isLocalDeployment = true
            LocalNimbusDeployment(packageName, stage, httpPort, webSocketPort)
            return instance
        }

        @JvmStatic
        fun getNewInstance(clazz: Class<out Any>): LocalNimbusDeployment {
            ClientBuilder.isLocalDeployment = true
            LocalNimbusDeployment(clazz)
            return instance
        }

        @JvmStatic
        fun getNewInstance(clazz: Class<out Any>, stage: String, port: Int, webSocketPort: Int): LocalNimbusDeployment {
            ClientBuilder.isLocalDeployment = true
            LocalNimbusDeployment(clazz, stage, port, webSocketPort)
            return instance
        }

        const val functionWebserverIdentifier = "function"
    }
}