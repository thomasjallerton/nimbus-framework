package testing

import annotation.annotations.function.QueueServerlessFunction
import org.reflections.Reflections
import org.reflections.util.ConfigurationBuilder
import org.reflections.util.FilterBuilder
import org.reflections.util.ClasspathHelper
import org.reflections.scanners.ResourcesScanner
import org.reflections.scanners.SubTypesScanner
import java.util.LinkedList


class LocalNimbusDeployment private constructor(packageName: String) {

    private val queues: MutableMap<String, LocalQueue> = mutableMapOf()
    private val methods: MutableMap<FunctionIdentifier, ServerlessMethod> = mutableMapOf()


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
        for (clazz in allClasses) {
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
            }
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
}