package com.nimbusframework.nimbuslocal.deployment.services.resource

import com.nimbusframework.nimbuscore.annotations.notification.NotificationTopicDefinition
import com.nimbusframework.nimbuslocal.deployment.notification.LocalNotificationTopic
import com.nimbusframework.nimbuslocal.deployment.services.LocalResourceHolder
import com.nimbusframework.nimbuslocal.deployment.services.StageService

class LocalNotificationTopicCreator(
        private val localResourceHolder: LocalResourceHolder,
        private val stageService: StageService
) : LocalCreateResourcesHandler {

    override fun createResource(clazz: Class<out Any>) {
        val notificationTopics = clazz.getAnnotationsByType(NotificationTopicDefinition::class.java)

        val annotation = stageService.annotationForStage(notificationTopics) { annotation -> annotation.stages}
        if (annotation != null) {
            localResourceHolder.notificationTopics[annotation.topicName] = LocalNotificationTopic()
        }

    }

}