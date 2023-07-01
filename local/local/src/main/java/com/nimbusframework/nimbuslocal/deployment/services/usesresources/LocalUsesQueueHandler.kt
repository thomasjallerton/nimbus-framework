package com.nimbusframework.nimbuslocal.deployment.services.usesresources

import com.nimbusframework.nimbuscore.annotations.queue.UsesQueue
import com.nimbusframework.nimbuscore.clients.queue.QueueIdAnnotationService
import com.nimbusframework.nimbuscore.permissions.PermissionType
import com.nimbusframework.nimbuslocal.deployment.function.FunctionEnvironment
import com.nimbusframework.nimbuslocal.deployment.function.permissions.QueuePermission
import com.nimbusframework.nimbuslocal.deployment.services.LocalResourceHolder
import com.nimbusframework.nimbuslocal.deployment.services.StageService
import java.lang.reflect.Method

class LocalUsesQueueHandler(
        localResourceHolder: LocalResourceHolder,
        private val stageService: StageService
) : LocalUsesResourcesHandler(localResourceHolder) {

    override fun handleUsesResources(clazz: Class<out Any>, method: Method, functionEnvironment: FunctionEnvironment) {
        val usesQueues = method.getAnnotationsByType(UsesQueue::class.java)

        val annotations = stageService.annotationsForStage(usesQueues) { annotation -> annotation.stages}
        for (annotation in annotations) {
            val queueId = QueueIdAnnotationService.getQueueId(annotation.queue.java, stageService.deployingStage)
            functionEnvironment.addPermission(PermissionType.QUEUE, QueuePermission(queueId))
        }
    }

}
