package com.nimbusframework.nimbuscore.clients.notification

import com.nimbusframework.nimbuscore.annotations.notification.NotificationTopicDefinition
import com.nimbusframework.nimbuscore.exceptions.InvalidStageException

object NotificationTopicAnnotationService {

    fun getTopicName(clazz: Class<*>, stage: String): String {
        val notificationTopicAnnotations = clazz.getDeclaredAnnotationsByType(NotificationTopicDefinition::class.java)
        for (notificationTopic in notificationTopicAnnotations) {
            for (annotationStage in notificationTopic.stages) {
                if (annotationStage == stage) {
                    return notificationTopic.topicName
                }
            }
        }
        throw InvalidStageException()
    }

}