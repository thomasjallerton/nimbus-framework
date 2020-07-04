package com.nimbusframework.nimbuscore.clients.queue

import com.nimbusframework.nimbuscore.annotations.queue.QueueDefinition
import com.nimbusframework.nimbuscore.exceptions.InvalidStageException

object QueueIdAnnotationService {

    fun getQueueId(clazz: Class<*>, stage: String): String {
        val queueAnnotations = clazz.getDeclaredAnnotationsByType(QueueDefinition::class.java)
        // Attempt to find specific annotation for this stage. If none exist then there is one annotation that has no stage (so uses the defaults)
        for (queue in queueAnnotations) {
            if (queue.stages.contains(stage)) {
                return queue.queueId
            }
        }
        val queue = queueAnnotations.firstOrNull { it.stages.isEmpty() } ?: throw InvalidStageException()
        return queue.queueId
    }


}