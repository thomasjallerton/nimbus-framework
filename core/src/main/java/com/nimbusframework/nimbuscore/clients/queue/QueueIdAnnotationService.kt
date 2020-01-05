package com.nimbusframework.nimbuscore.clients.queue

import com.nimbusframework.nimbuscore.annotations.queue.QueueDefinition
import com.nimbusframework.nimbuscore.exceptions.InvalidStageException

object QueueIdAnnotationService {

    fun getQueueId(clazz: Class<*>, stage: String): String {
        val queueAnnotations = clazz.getDeclaredAnnotationsByType(QueueDefinition::class.java)
        for (queue in queueAnnotations) {
            for (annotationStage in queue.stages) {
                if (annotationStage == stage) {
                    return queue.queueId
                }
            }
        }
        throw InvalidStageException()
    }

}