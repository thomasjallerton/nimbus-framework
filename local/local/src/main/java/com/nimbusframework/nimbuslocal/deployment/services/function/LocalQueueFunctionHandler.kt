package com.nimbusframework.nimbuslocal.deployment.services.function

import com.nimbusframework.nimbuscore.annotations.function.QueueServerlessFunction
import com.nimbusframework.nimbuscore.clients.queue.QueueIdAnnotationService
import com.nimbusframework.nimbuslocal.deployment.function.FunctionIdentifier
import com.nimbusframework.nimbuslocal.deployment.function.ServerlessFunction
import com.nimbusframework.nimbuslocal.deployment.function.information.QueueFunctionInformation
import com.nimbusframework.nimbuslocal.deployment.queue.QueueMethod
import com.nimbusframework.nimbuslocal.deployment.services.LocalResourceHolder
import com.nimbusframework.nimbuslocal.deployment.services.StageService
import java.lang.reflect.Method

class LocalQueueFunctionHandler(
        private val localResourceHolder: LocalResourceHolder,
        private val stageService: StageService
) : LocalFunctionHandler(localResourceHolder) {

    override fun handleMethod(clazz: Class<out Any>, method: Method): Boolean {
        val queueServerlessFunctions = method.getAnnotationsByType(QueueServerlessFunction::class.java)
        if (queueServerlessFunctions.isEmpty()) return false

        val functionIdentifier = FunctionIdentifier(clazz.canonicalName, method.name)

        val annotation = stageService.annotationForStage(queueServerlessFunctions) {annotation -> annotation.stages}
        if (annotation != null) {
            val invokeOn = getFunctionClassInstance(clazz)

            val queueMethod = QueueMethod(method, invokeOn, annotation.batchSize)
            val queueId = QueueIdAnnotationService.getQueueId(annotation.queue.java, stageService.deployingStage)

            val functionInformation = QueueFunctionInformation(queueId, annotation.batchSize)
            val queue = localResourceHolder.queues[queueId]!!

            queue.addConsumer(queueMethod)
            localResourceHolder.functions[functionIdentifier] = ServerlessFunction(queueMethod, functionInformation)
        }
        return true
    }

}