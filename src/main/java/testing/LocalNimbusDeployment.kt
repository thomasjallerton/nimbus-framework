package testing

import annotation.annotations.function.HttpServerlessFunction
import annotation.annotations.function.QueueServerlessFunction
import annotation.annotations.keyvalue.KeyValueStore
import clients.keyvalue.KeyValueStoreClient
import clients.keyvalue.KeyValueStoreClientLocal
import org.reflections.Reflections
import org.reflections.util.ConfigurationBuilder
import org.reflections.util.FilterBuilder
import org.reflections.util.ClasspathHelper
import org.reflections.scanners.ResourcesScanner
import org.reflections.scanners.SubTypesScanner
import sun.security.krb5.internal.PAData
import java.lang.reflect.InvocationTargetException
import java.util.LinkedList


class LocalNimbusDeployment private constructor(packageName: String) {

    private val queues: MutableMap<String, LocalQueue> = mutableMapOf()
    private val methods: MutableMap<FunctionIdentifier, ServerlessMethod> = mutableMapOf()
    private val httpMethods: MutableMap<HttpMethodIdentifier, HttpMethod> = mutableMapOf()
    private val keyValueStores: MutableMap<String, MutableMap<Any?, Any?>> = mutableMapOf()

    init {
        instance = this
        val classLoadersList = LinkedList<ClassLoader>()
        classLoadersList.add(ClasspathHelper.contextClassLoader())
        classLoadersList.add(ClasspathHelper.staticClassLoader())

        val reflections = Reflections(ConfigurationBuilder()
                .setScanners(SubTypesScanner(false), ResourcesScanner())
                .setUrls(ClasspathHelper.forClassLoader(*classLoadersList.toTypedArray()))
                .filterInputsBy(FilterBuilder().include(FilterBuilder.prefix(packageName))))

        val allClasses = reflections.getSubTypesOf(Any::class.java)

        //Handle Resources that need to exist for handlers to work
        for (clazz in allClasses) {
            if (clazz.isAnnotationPresent(KeyValueStore::class.java)) {
                val tableName = KeyValueStoreClient.getTableName(clazz)

                keyValueStores[tableName] = mutableMapOf()
            }
        }

        //Handle handlers
        for (clazz in allClasses) {
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
                }
            } catch (e: InvocationTargetException) {
                System.err.println("Error creating handler class, it should have no constructor parameters")
                e.targetException.printStackTrace()
            }

        }
    }

    internal fun <T> getKeyValueStore(clazz: Class<T>): MutableMap<Any?, Any?> {
        val tableName = KeyValueStoreClient.getTableName(clazz)
        if (keyValueStores.containsKey(tableName)) {
            return keyValueStores[tableName]!!
        } else {
            throw ResourceNotFoundException()
        }
    }

    fun <K, V> getKeyValueStoreClient(keyClass: Class<K>, valueClass: Class<V>): KeyValueStoreClient<K, V> {
        return KeyValueStoreClientLocal(keyClass, valueClass)
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
            LocalNimbusDeployment(packageName)
            return instance
        }
    }

    private data class FunctionIdentifier(val className: String, val methodName: String)

    private data class HttpMethodIdentifier(val path: String, val method: String)
}