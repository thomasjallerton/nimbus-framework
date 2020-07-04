package com.nimbusframework.nimbuscore.clients.notification

import com.nimbusframework.nimbuscore.annotations.notification.NotificationTopicDefinition
import com.nimbusframework.nimbuscore.exceptions.InvalidStageException

object NotificationTopicAnnotationService {

    fun getTopicName(clazz: Class<*>, stage: String): String {
        val notificationTopicAnnotations = clazz.getDeclaredAnnotationsByType(NotificationTopicDefinition::class.java)
        // Attempt to find specific annotation for this stage. If none exist then there is one annotation that has no stage (so uses the defaults)
        for (notificationTopic in notificationTopicAnnotations) {
            if (notificationTopic.stages.contains(stage)) {
                return notificationTopic.topicName
            }
        }
        val notificationTopic = notificationTopicAnnotations.firstOrNull { it.stages.isEmpty() } ?: throw InvalidStageException()
        return notificationTopic.topicName
    }

}