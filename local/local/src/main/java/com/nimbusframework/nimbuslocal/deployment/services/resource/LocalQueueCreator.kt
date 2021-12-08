package com.nimbusframework.nimbuslocal.deployment.services.resource

import com.nimbusframework.nimbuscore.annotations.queue.QueueDefinition
import com.nimbusframework.nimbuslocal.deployment.queue.LocalQueue
import com.nimbusframework.nimbuslocal.deployment.services.LocalResourceHolder
import com.nimbusframework.nimbuslocal.deployment.services.StageService

class LocalQueueCreator(
        private val localResourceHolder: LocalResourceHolder,
        private val stageService: StageService
) : LocalCreateResourcesHandler {

    override fun createResource(clazz: Class<out Any>) {
        val queues = clazz.getAnnotationsByType(QueueDefinition::class.java)

        val annotation = stageService.annotationForStage(queues) { annotation -> annotation.stages}
        if (annotation != null) {
            localResourceHolder.queues[annotation.queueId] = LocalQueue()
        }
    }

}