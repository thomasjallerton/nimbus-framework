package testing

import annotation.annotations.document.DocumentStore
import annotation.annotations.function.DocumentStoreServerlessFunction
import annotation.annotations.function.HttpServerlessFunction
import annotation.annotations.function.KeyValueStoreServerlessFunction
import annotation.annotations.function.QueueServerlessFunction
import annotation.annotations.keyvalue.KeyValueStore
import clients.document.DocumentStoreClient
import clients.keyvalue.KeyValueStoreClient
import clients.keyvalue.KeyValueStoreClientLocal
import org.reflections.Reflections
import org.reflections.scanners.ResourcesScanner
import org.reflections.scanners.SubTypesScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import org.reflections.util.FilterBuilder
import testing.document.KeyValueMethod
import testing.document.LocalDocumentStore
import testing.http.HttpMethod
import testing.http.HttpRequest
import testing.keyvalue.LocalKeyValueStore
import testing.queue.LocalQueue
import testing.queue.QueueMethod
import java.lang.reflect.InvocationTargetException
import java.util.*


class LocalNimbusDeployment {

    private val queues: MutableMap<String, LocalQueue> = mutableMapOf()
    private val methods: MutableMap<FunctionIdentifier, ServerlessMethod> = mutableMapOf()
    private val httpMethods: MutableMap<HttpMethodIdentifier, HttpMethod> = mutableMapOf()
    private val keyValueStores: MutableMap<String, LocalKeyValueStore<out Any, out Any>> = mutableMapOf()
    private val documentStores: MutableMap<String, LocalDocumentStore<out Any>> = mutableMapOf()

    private constructor(clazz: Class<out Any>) {
        createResources(clazz)
        createHandlers(clazz)
    }

    private constructor(packageName: String) {
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
    }

    private fun createResources(clazz: Class<out Any>) {
        if (clazz.isAnnotationPresent(KeyValueStore::class.java)) {
            val tableName = KeyValueStoreClient.getTableName(clazz)
            val annotation = clazz.getDeclaredAnnotation(KeyValueStore::class.java)
            keyValueStores[tableName] = LocalKeyValueStore(annotation.keyType.java, clazz)
        }
        if (clazz.isAnnotationPresent(DocumentStore::class.java)) {
            val tableName = DocumentStoreClient.getTableName(clazz)
            documentStores[tableName] = LocalDocumentStore(clazz)
        }
    }

    private fun createHandlers(clazz: Class<out Any>) {
        try {
            for (method in clazz.declaredMethods) {
                val functionIdentifier = FunctionIdentifier(clazz.canonicalName, method.name)
                if (method.isAnnotationPresent(QueueServerlessFunction::class.java)) {
                    val queueServerlessFunctions = method.getAnnotationsByType(QueueServerlessFunction::class.java)

                    val invokeOn = clazz.getConstructor().newInstance()
                    for (queueFunction in queueServerlessFunctions) {
                        val queueMethod = QueueMethod(method, invokeOn, queueFunction.batchSize)
                        val newQueue = LocalQueue(queueMethod)
                        queues[queueFunction.id] = newQueue
                        methods[functionIdentifier] = queueMethod
                    }
                }

                if (method.isAnnotationPresent(HttpServerlessFunction::class.java)) {
                    val httpServerlessFunctions = method.getAnnotationsByType(HttpServerlessFunction::class.java)

                    val invokeOn = clazz.getConstructor().newInstance()
                    for (httpFunction in httpServerlessFunctions) {
                        val httpMethod = HttpMethod(method, invokeOn)
                        val httpIdentifier = HttpMethodIdentifier(httpFunction.path, httpFunction.method)
                        httpMethods[httpIdentifier] = httpMethod
                        methods[functionIdentifier] = httpMethod
                    }
                }

                if (method.isAnnotationPresent(DocumentStoreServerlessFunction::class.java)) {
                    val documentFunctions = method.getAnnotationsByType(DocumentStoreServerlessFunction::class.java)

                    val invokeOn = clazz.getConstructor().newInstance()
                    for (documentFunction in documentFunctions) {
                        val documentMethod = KeyValueMethod(method, invokeOn, documentFunction.method)
                        methods[functionIdentifier] = documentMethod
                        val tableName = DocumentStoreClient.getTableName(documentFunction.dataModel.java)
                        val documentStore = documentStores[tableName]
                        documentStore?.addMethod(documentMethod)
                    }
                }

                if (method.isAnnotationPresent(KeyValueStoreServerlessFunction::class.java)) {
                    val keyValueFunctions = method.getAnnotationsByType(KeyValueStoreServerlessFunction::class.java)

                    val invokeOn = clazz.getConstructor().newInstance()
                    for (keyValueFunction in keyValueFunctions) {
                        val documentMethod = KeyValueMethod(method, invokeOn, keyValueFunction.method)
                        methods[functionIdentifier] = documentMethod
                        val tableName = KeyValueStoreClient.getTableName(keyValueFunction.dataModel.java)
                        val keyValueStore = keyValueStores[tableName]
                        keyValueStore?.addMethod(documentMethod)
                    }
                }
            }
        } catch (e: InvocationTargetException) {
            System.err.println("Error creating handler class, it should have no constructor parameters")
            e.targetException.printStackTrace()
        }
    }

    fun <K, V> getKeyValueStore(valueClass: Class<V>): LocalKeyValueStore<K, V> {
        val tableName = KeyValueStoreClient.getTableName(valueClass)
        if (keyValueStores.containsKey(tableName)) {
            return keyValueStores[tableName] as LocalKeyValueStore<K, V>
        } else {
            throw ResourceNotFoundException()
        }
    }

    fun <K, V> getKeyValueStoreClient(keyClass: Class<K>, valueClass: Class<V>): KeyValueStoreClient<K, V> {
        return KeyValueStoreClientLocal(keyClass, valueClass)
    }

    fun <T> getDocumentStore(clazz: Class<T>): LocalDocumentStore<T> {
        val tableName = DocumentStoreClient.getTableName(clazz)
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

    fun <T> getMethod(clazz: Class<T>, methodName: String): ServerlessMethod {
        val functionIdentifier = FunctionIdentifier(clazz.canonicalName, methodName)
        if (methods.containsKey(functionIdentifier)) {
            return methods[functionIdentifier]!!
        } else {
            throw ResourceNotFoundException()
        }
    }

    fun sendHttpReguest(request: HttpRequest) {
        val httpIdentifier = HttpMethodIdentifier(request.path, request.method)
        if (httpMethods.containsKey(httpIdentifier)) {
            httpMethods[httpIdentifier]!!.invoke(request)
        } else {
            throw ResourceNotFoundException()
        }
    }

    companion object {
        private lateinit var instance: LocalNimbusDeployment
        internal var isLocalDeployment: Boolean = false

        @JvmStatic
        fun getInstance(): LocalNimbusDeployment {
            return instance
        }

        @JvmStatic
        fun getNewInstance(packageName: String): LocalNimbusDeployment {
            isLocalDeployment = true
            instance = LocalNimbusDeployment(packageName)
            return instance
        }

        @JvmStatic
        fun getNewInstance(clazz: Class<out Any>): LocalNimbusDeployment {
            isLocalDeployment = true
            instance = LocalNimbusDeployment(clazz)
            return instance
        }
    }

    private data class FunctionIdentifier(val className: String, val methodName: String)

    private data class HttpMethodIdentifier(val path: String, val method: String)
}