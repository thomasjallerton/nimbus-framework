package com.nimbusframework.nimbuscore.testing.services.function

import com.nimbusframework.nimbuscore.annotation.annotations.function.QueueServerlessFunction
import com.nimbusframework.nimbuscore.testing.function.FunctionIdentifier
import com.nimbusframework.nimbuscore.testing.queue.LocalQueue
import com.nimbusframework.nimbuscore.testing.queue.QueueMethod
import com.nimbusframework.nimbuscore.testing.services.LocalResourceHolder
import java.lang.reflect.Method

class LocalQueueFunctionHandler(
        private val localResourceHolder: LocalResourceHolder,
        private val stage: String
) : LocalFunctionHandler(localResourceHolder) {

    override fun handleMethod(clazz: Class<out Any>, method: Method) {
        val functionIdentifier = FunctionIdentifier(clazz.canonicalName, method.name)

        val queueServerlessFunctions = method.getAnnotationsByType(QueueServerlessFunction::class.java)

        for (queueFunction in queueServerlessFunctions) {
            if (queueFunction.stages.contains(stage)) {
                val invokeOn = clazz.getConstructor().newInstance()

                val queueMethod = QueueMethod(method, invokeOn, queueFunction.batchSize)
                val newQueue = LocalQueue(queueMethod)
                localResourceHolder.queues[queueFunction.id] = newQueue
                localResourceHolder.methods[functionIdentifier] = queueMethod
            }
        }
    }

}