package testing

import annotation.annotations.deployment.AfterDeployment
import annotation.annotations.deployment.FileUpload
import annotation.annotations.document.DocumentStore
import annotation.annotations.file.FileStorageBucket
import annotation.annotations.file.UsesFileStorageClient
import annotation.annotations.function.*
import annotation.annotations.keyvalue.KeyValueStore
import annotation.annotations.notification.UsesNotificationTopic
import clients.ClientBuilder
import clients.document.DocumentStoreClient
import clients.file.FileStorageClient
import clients.keyvalue.KeyValueStoreClient
import clients.keyvalue.KeyValueStoreClientLocal
import clients.rdbms.DatabaseClient
import clients.rdbms.DatabaseClientLocal
import org.reflections.Reflections
import org.reflections.scanners.ResourcesScanner
import org.reflections.scanners.SubTypesScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import org.reflections.util.FilterBuilder
import persisted.FileUploadDescription
import testing.basic.BasicMethod
import testing.document.KeyValueMethod
import testing.document.LocalDocumentStore
import testing.file.FileStorageMethod
import testing.file.LocalFileStorage
import testing.http.LocalHttpMethod
import testing.http.HttpRequest
import testing.keyvalue.LocalKeyValueStore
import testing.notification.LocalNotificationTopic
import testing.notification.NotificationMethod
import testing.queue.LocalQueue
import testing.queue.QueueMethod
import testing.webserver.LocalWebserver
import testing.webserver.WebserverHandler
import testing.websocket.LocalWebsocketMethod
import testing.websocket.WebSocketRequest
import java.io.File
import java.io.InputStream
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.nio.charset.StandardCharsets
import java.util.*


class LocalNimbusDeployment {

    private val queues: MutableMap<String, LocalQueue> = mutableMapOf()
    private val methods: MutableMap<FunctionIdentifier, ServerlessMethod> = mutableMapOf()
    private val localHttpMethods: MutableMap<HttpMethodIdentifier, LocalHttpMethod> = mutableMapOf()
    private val localWebsocketMethods: MutableMap<String, LocalWebsocketMethod> = mutableMapOf()
    private val localBasicMethods: MutableMap<FunctionIdentifier, BasicMethod> = mutableMapOf()
    private val keyValueStores: MutableMap<String, LocalKeyValueStore<out Any, out Any>> = mutableMapOf()
    private val documentStores: MutableMap<String, LocalDocumentStore<out Any>> = mutableMapOf()
    private val fileStorage: MutableMap<String, LocalFileStorage> = mutableMapOf()
    private val notificationTopics: MutableMap<String, LocalNotificationTopic> = mutableMapOf()
    private val afterDeployments: Deque<Pair<Method, Any>> = LinkedList()
    private val localWebservers: MutableMap<String, WebserverHandler> = mutableMapOf()
    private val functionWebserverIdentifier = "function"
    private val port: Int

    private val variableSubstitution: MutableMap<String, String> = mutableMapOf()
    private val fileUploadDetails: MutableMap<String, MutableList<FileUploadDescription>> = mutableMapOf()

    private constructor(clazz: Class<out Any>, stageParam: String = "dev", port: Int = 8080) {
        instance = this
        this.port = port
        createResources(clazz)
        createHandlers(clazz)
        stage = stageParam

        afterDeployments.forEach { (method, obj) -> method.invoke(obj) }

        handleUploadingFile(fileUploadDetails)
    }

    private constructor(packageName: String, stageParam: String = "dev", port: Int = 8080) {
        instance = this
        stage = stageParam
        this.port = port

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

        //Handle handlers
        allClasses.forEach { clazz -> createHandlers(clazz) }

        afterDeployments.forEach { (method, obj) -> method.invoke(obj) }

        handleUploadingFile(fileUploadDetails)
    }

    private fun handleUploadingFile(bucketUploads: Map<String, List<FileUploadDescription>>) {
        for ((bucketName, fileUploads) in bucketUploads) {
            val fileStorageClient = ClientBuilder.getFileStorageClient(bucketName)

            for ((localFile, targetFile, substituteVariables) in fileUploads) {
                val file = File(localFile)

                if (file.isFile) {
                    if (substituteVariables) {
                        fileStorageClient.saveFile(targetFile, substituteVariables(file))
                    } else {
                        fileStorageClient.saveFile(targetFile, file)
                    }
                } else if (file.isDirectory){
                    val newPath = if (targetFile.endsWith("/") || targetFile.isEmpty()) {
                        targetFile
                    } else {
                        "$targetFile/"
                    }
                    uploadDirectory(fileStorageClient, file, newPath, substituteVariables)
                }
            }
        }
    }

    private fun uploadDirectory(fileStorageClient: FileStorageClient, directory: File, s3Path: String, substituteVariables: Boolean) {
        for (file in directory.listFiles()) {
            val newPath = if (s3Path.isEmpty()) {
                file.name
            } else {
                "$s3Path/${file.name}"
            }

            if (file.isFile) {
                if (substituteVariables) {
                    fileStorageClient.saveFile(newPath, substituteVariables(file))
                } else {
                    fileStorageClient.saveFile(newPath, file)
                }
            } else if (file.isDirectory){
                uploadDirectory(fileStorageClient, file, newPath, substituteVariables)
            }
        }
    }

    private fun substituteVariables(file: File): InputStream {
        val charset = StandardCharsets.UTF_8

        var content = String(file.readBytes(), charset)
        for ((from, to) in variableSubstitution) {
            content = content.replace(from, to)
        }

        return content.byteInputStream(charset)
    }

    private fun createResources(clazz: Class<out Any>) {

        val keyValueStoreAnnotations = clazz.getAnnotationsByType(KeyValueStore::class.java)

        for (keyValueStoreAnnotation in keyValueStoreAnnotations) {
            val tableName = KeyValueStoreClient.getTableName(clazz, stage)
            val annotation = clazz.getDeclaredAnnotation(KeyValueStore::class.java)
            keyValueStores[tableName] = LocalKeyValueStore(annotation.keyType.java, clazz, stage)
        }

        val documentStoreAnnotations = clazz.getAnnotationsByType(DocumentStore::class.java)

        for (documentStoreAnnotation in documentStoreAnnotations) {
            val tableName = DocumentStoreClient.getTableName(clazz, stage)
            documentStores[tableName] = LocalDocumentStore(clazz, stage)
        }


        val fileStorageBuckets = clazz.getAnnotationsByType(FileStorageBucket::class.java)

        for (fileStorageBucket in fileStorageBuckets) {
            if (fileStorageBucket.staticWebsite && !localWebservers.containsKey(fileStorageBucket.bucketName)) {
                val localWebserver = WebserverHandler(fileStorageBucket.indexFile, fileStorageBucket.errorFile)
                localWebservers[fileStorageBucket.bucketName] = localWebserver
                variableSubstitution["\${${fileStorageBucket.bucketName.toUpperCase()}_URL}"] = "http://localhost:$port/${fileStorageBucket.bucketName}"
            }

            if (!fileStorage.containsKey(fileStorageBucket.bucketName)) {
                fileStorage[fileStorageBucket.bucketName] = LocalFileStorage(fileStorageBucket.bucketName)
            }
        }

        handleFileUpload(clazz.getAnnotationsByType(FileUpload::class.java))

        for (method in clazz.methods) {
            val usesNotificationTopics = method.getAnnotationsByType(UsesNotificationTopic::class.java)

            for (usesNotificationTopic in usesNotificationTopics) {
                notificationTopics.putIfAbsent(usesNotificationTopic.topic, LocalNotificationTopic())
            }

            val usesFileStorages = method.getAnnotationsByType(UsesFileStorageClient::class.java)

            for (usesFileStorage in usesFileStorages) {
                if (!fileStorage.containsKey(usesFileStorage.bucketName)) {
                    fileStorage[usesFileStorage.bucketName] = LocalFileStorage(usesFileStorage.bucketName)
                }
            }

            handleFileUpload(method.getAnnotationsByType(FileUpload::class.java))
        }
    }

    private fun handleFileUpload(fileUploads: Array<out FileUpload>) {

        for (fileUpload in fileUploads) {
            val bucketFiles = fileUploadDetails.getOrPut(fileUpload.bucketName) { mutableListOf() }
            val description = FileUploadDescription(fileUpload.localPath, fileUpload.targetPath, fileUpload.substituteNimbusVariables)
            bucketFiles.add(description)
        }

    }


    private fun createHandlers(clazz: Class<out Any>) {
        try {
            for (method in clazz.declaredMethods) {
                val functionIdentifier = FunctionIdentifier(clazz.canonicalName, method.name)
                val invokeOn = clazz.getConstructor().newInstance()

                val fileStorageFunctions = method.getAnnotationsByType(FileStorageServerlessFunction::class.java)

                for (fileStorageFunction in fileStorageFunctions) {
                    if (!fileStorage.containsKey(fileStorageFunction.bucketName)) {
                        fileStorage[fileStorageFunction.bucketName] = LocalFileStorage(fileStorageFunction.bucketName)
                    }
                    val localFileStorage = fileStorage[fileStorageFunction.bucketName]
                    val fileStorageMethod = FileStorageMethod(method, invokeOn, fileStorageFunction.eventType)
                    localFileStorage!!.addMethod(fileStorageMethod)
                    methods[functionIdentifier] = fileStorageMethod

                }

                val queueServerlessFunctions = method.getAnnotationsByType(QueueServerlessFunction::class.java)

                for (queueFunction in queueServerlessFunctions) {
                    val queueMethod = QueueMethod(method, invokeOn, queueFunction.batchSize)
                    val newQueue = LocalQueue(queueMethod)
                    queues[queueFunction.id] = newQueue
                    methods[functionIdentifier] = queueMethod
                }

                val basicServerlessFunctions = method.getAnnotationsByType(BasicServerlessFunction::class.java)

                for (basicFunction in basicServerlessFunctions) {
                    val basicMethod = BasicMethod(method, invokeOn)
                    methods[functionIdentifier] = basicMethod
                    localBasicMethods[functionIdentifier] = basicMethod
                }

                val httpServerlessFunctions = method.getAnnotationsByType(HttpServerlessFunction::class.java)

                for (httpFunction in httpServerlessFunctions) {
                    val httpMethod = LocalHttpMethod(method, invokeOn)
                    val httpIdentifier = HttpMethodIdentifier(httpFunction.path, httpFunction.method)
                    localHttpMethods[httpIdentifier] = httpMethod
                    methods[functionIdentifier] = httpMethod

                    val lambdaWebserver = localWebservers.getOrPut(functionWebserverIdentifier) {
                        variableSubstitution["\${NIMBUS_REST_API_URL}"] = "http://localhost:$port/$functionWebserverIdentifier"
                        WebserverHandler("", "")
                    }

                    lambdaWebserver.addWebResource(httpFunction.path, httpFunction.method, httpMethod)
                }

                val webSocketServerlessFunctions = method.getAnnotationsByType(WebSocketServerlessFunction::class.java)

                for (webSocketFunction in webSocketServerlessFunctions) {
                    val webSocketMethod = LocalWebsocketMethod(method, invokeOn)

                    localWebsocketMethods[webSocketFunction.routeKey] = webSocketMethod
                    methods[functionIdentifier] = webSocketMethod

                    variableSubstitution["\${NIMBUS_WEBSOCKET_API_URL}"] = "wss://localhost"
                }

                val documentFunctions = method.getAnnotationsByType(DocumentStoreServerlessFunction::class.java)

                for (documentFunction in documentFunctions) {
                    val documentMethod = KeyValueMethod(method, invokeOn, documentFunction.method)
                    methods[functionIdentifier] = documentMethod
                    val tableName = DocumentStoreClient.getTableName(documentFunction.dataModel.java, stage)
                    val documentStore = documentStores[tableName]
                    documentStore?.addMethod(documentMethod)
                }

                val keyValueFunctions = method.getAnnotationsByType(KeyValueStoreServerlessFunction::class.java)

                for (keyValueFunction in keyValueFunctions) {
                    val documentMethod = KeyValueMethod(method, invokeOn, keyValueFunction.method)
                    methods[functionIdentifier] = documentMethod
                    val tableName = KeyValueStoreClient.getTableName(keyValueFunction.dataModel.java, stage)
                    val keyValueStore = keyValueStores[tableName]
                    keyValueStore?.addMethod(documentMethod)
                }

                val notificationServerlessFunctions = method.getAnnotationsByType(NotificationServerlessFunction::class.java)

                for (notificationFunction in notificationServerlessFunctions) {
                    val notificationMethod = NotificationMethod(method, invokeOn)

                    val notificationTopic = notificationTopics.getOrPut(notificationFunction.topic) { LocalNotificationTopic() }

                    notificationTopic.addSubscriber(notificationMethod)
                    methods[functionIdentifier] = notificationMethod
                }

                if (method.isAnnotationPresent(AfterDeployment::class.java)) {
                    val afterDeployment = method.getAnnotation(AfterDeployment::class.java)

                    val invokeOn = clazz.getConstructor().newInstance()

                    if (afterDeployment.isTest) {
                        afterDeployments.addLast(Pair(method, invokeOn))
                    } else {
                        afterDeployments.addFirst(Pair(method, invokeOn))
                    }
                }
            }
        } catch (e: InvocationTargetException) {
            System.err.println("Error creating handler class ${clazz.canonicalName}, it should have no constructor parameters")
            e.targetException.printStackTrace()
        }
    }

    internal fun getLocalHandler(bucketName: String): WebserverHandler? {
        return localWebservers[bucketName]
    }

    fun startWebserver(bucketName: String) {
        if (localWebservers.containsKey(bucketName)) {
            val handler = localWebservers[bucketName]!!
            val webserver = LocalWebserver()
            webserver.handler.addResource(bucketName, handler)
            webserver.startServer(port)        } else {
            throw ResourceNotFoundException()
        }
    }

    fun startServerlessFunctionWebserver() {
        if (localWebservers.containsKey(functionWebserverIdentifier)) {
            val handler = localWebservers[functionWebserverIdentifier]!!
            val webserver = LocalWebserver()
            webserver.handler.addResource(functionWebserverIdentifier, handler)
            webserver.startServer(port)
        } else {
            throw ResourceNotFoundException()
        }
    }

    fun startAllWebservers() {
        val allResourcesWebserver = LocalWebserver()
        for ((identifier, handler) in localWebservers) {
            allResourcesWebserver.handler.addResource(identifier, handler)
        }
        allResourcesWebserver.startServer(port)
    }


    fun <K, V> getKeyValueStore(valueClass: Class<V>): LocalKeyValueStore<K, V> {
        val tableName = KeyValueStoreClient.getTableName(valueClass, stage)
        if (keyValueStores.containsKey(tableName)) {
            return keyValueStores[tableName] as LocalKeyValueStore<K, V>
        } else {
            throw ResourceNotFoundException()
        }
    }

    fun <K, V> getKeyValueStoreClient(keyClass: Class<K>, valueClass: Class<V>): KeyValueStoreClient<K, V> {
        return KeyValueStoreClientLocal(keyClass, valueClass, stage)
    }

    fun <T> getRelationalDatabaseClient(dataClass: Class<T>): DatabaseClient {
        return DatabaseClientLocal(dataClass)
    }

    fun getLocalFileStorage(bucketName: String): LocalFileStorage {
        if (fileStorage.containsKey(bucketName)) {
            return fileStorage[bucketName]!!
        } else {
            throw ResourceNotFoundException()
        }
    }

    fun <T> getDocumentStore(clazz: Class<T>): LocalDocumentStore<T> {
        val tableName = DocumentStoreClient.getTableName(clazz, stage)
        if (documentStores.containsKey(tableName)) {
            return documentStores[tableName] as LocalDocumentStore<T>
        } else {
            throw ResourceNotFoundException()
        }
    }

    fun getQueue(id: String): LocalQueue {
        if (queues.containsKey(id)) {
            return queues[id]!!
        } else {
            throw ResourceNotFoundException()
        }
    }

    fun getNotificationTopic(topic: String): LocalNotificationTopic {
        if (notificationTopics.containsKey(topic)) {
            return notificationTopics[topic]!!
        } else {
            throw ResourceNotFoundException()
        }
    }

    fun <T> getMethod(clazz: Class<T>, methodName: String): ServerlessMethod {
        val functionIdentifier = FunctionIdentifier(clazz.canonicalName, methodName)
        if (methods.containsKey(functionIdentifier)) {
            return methods[functionIdentifier]!!
        } else {
            throw ResourceNotFoundException()
        }
    }

    fun <T> getBasicMethod(clazz: Class<T>, methodName: String): BasicMethod {
        val functionIdentifier = FunctionIdentifier(clazz.canonicalName, methodName)
        if (localBasicMethods.containsKey(functionIdentifier)) {
            return localBasicMethods[functionIdentifier]!!
        } else {
            throw ResourceNotFoundException()
        }
    }

    fun sendHttpReguest(request: HttpRequest) {
        val httpIdentifier = HttpMethodIdentifier(request.path, request.method)
        if (localHttpMethods.containsKey(httpIdentifier)) {
            localHttpMethods[httpIdentifier]!!.invoke(request)
        } else {
            throw ResourceNotFoundException()
        }
    }

    fun connectToWebSockets(headers: Map<String, String> = mapOf(), queryStringParams: Map<String, String> = mapOf()) {
        val topic = "\$connect"
        val request = WebSocketRequest("{\"topic\":\"\$connect\"}", queryStringParams, headers)
        if (localWebsocketMethods.containsKey(topic)) {
            localWebsocketMethods[topic]!!.invoke(request)
        } else {
            throw ResourceNotFoundException()
        }
    }

    fun disconnectFromWebSockets() {
        val topic = "\$disconnect"
        val request = WebSocketRequest("{\"topic\":\"\$disconnect\"}")
        if (localWebsocketMethods.containsKey(topic)) {
            localWebsocketMethods[topic]!!.invoke(request)
        } else {
            throw ResourceNotFoundException()
        }
    }

    fun sendWebSocketRequest(request: WebSocketRequest) {
        val topic = request.getTopic()
        if (localWebsocketMethods.containsKey(topic)) {
            localWebsocketMethods[topic]!!.invoke(request)
        } else {
            throw ResourceNotFoundException()
        }
    }

    companion object {
        private lateinit var instance: LocalNimbusDeployment
        internal lateinit var stage: String
        internal var isLocalDeployment: Boolean = false

        @JvmStatic
        fun getInstance(): LocalNimbusDeployment {
            return instance
        }

        @JvmStatic
        fun getNewInstance(packageName: String): LocalNimbusDeployment {
            isLocalDeployment = true
            LocalNimbusDeployment(packageName)
            return instance
        }

        @JvmStatic
        fun getNewInstance(packageName: String, stage: String, port: Int): LocalNimbusDeployment {
            isLocalDeployment = true
            LocalNimbusDeployment(packageName, stage, port)
            return instance
        }

        @JvmStatic
        fun getNewInstance(clazz: Class<out Any>): LocalNimbusDeployment {
            isLocalDeployment = true
            LocalNimbusDeployment(clazz)
            return instance
        }

        @JvmStatic
        fun getNewInstance(clazz: Class<out Any>, stage: String, port: Int): LocalNimbusDeployment {
            isLocalDeployment = true
            LocalNimbusDeployment(clazz, stage, port)
            return instance
        }
    }

    private data class FunctionIdentifier(val className: String, val methodName: String)

    private data class HttpMethodIdentifier(val path: String, val method: HttpMethod)
}