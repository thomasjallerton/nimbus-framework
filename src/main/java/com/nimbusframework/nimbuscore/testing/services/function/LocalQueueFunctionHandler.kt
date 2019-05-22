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

    override fun handleMethod(clazz: Class<out Any>, method: Method): Boolean {
        val functionIdentifier = FunctionIdentifier(clazz.canonicalName, method.name)

        val queueServerlessFunctions = method.getAnnotationsByType(QueueServerlessFunction::class.java)
        if (queueServerlessFunctions.isEmpty()) return false

        for (queueFunction in queueServerlessFunctions) {
            if (queueFunction.stages.contains(stage)) {
                val invokeOn = clazz.getConstructor().newInstance()

                val queueMethod = QueueMethod(method, invokeOn, queueFunction.batchSize)
                val queue = if (localResourceHolder.queues.containsKey(queueFunction.id)) {
                    localResourceHolder.queues[queueFunction.id]!!
                } else {
                    val newQueue = LocalQueue()
                    localResourceHolder.queues[queueFunction.id] = newQueue
                    newQueue
                }
                queue.addConsumer(queueMethod)
                localResourceHolder.methods[functionIdentifier] = queueMethod
            }
        }
        return true
    }

}