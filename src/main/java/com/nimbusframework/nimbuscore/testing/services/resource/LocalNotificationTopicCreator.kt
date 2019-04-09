package com.nimbusframework.nimbuscore.testing.services.resource

import com.nimbusframework.nimbuscore.annotation.annotations.notification.UsesNotificationTopic
import com.nimbusframework.nimbuscore.testing.notification.LocalNotificationTopic
import com.nimbusframework.nimbuscore.testing.services.LocalResourceHolder

class LocalNotificationTopicCreator(
        private val localResourceHolder: LocalResourceHolder,
        private val stage: String
): LocalCreateResourcesHandler {

    override fun createResource(clazz: Class<out Any>) {
        for (method in clazz.methods) {
            val usesNotificationTopics = method.getAnnotationsByType(UsesNotificationTopic::class.java)

            for (usesNotificationTopic in usesNotificationTopics) {
                if (usesNotificationTopic.stages.contains(stage)) {
                    localResourceHolder.notificationTopics.putIfAbsent(usesNotificationTopic.topic, LocalNotificationTopic())
                }
            }
        }
    }

}