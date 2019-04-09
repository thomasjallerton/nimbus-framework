package com.nimbusframework.nimbuscore.testing.services.usesresources

import com.nimbusframework.nimbuscore.annotation.annotations.notification.UsesNotificationTopic
import com.nimbusframework.nimbuscore.testing.function.FunctionEnvironment
import com.nimbusframework.nimbuscore.testing.function.PermissionType
import com.nimbusframework.nimbuscore.testing.function.permissions.NotificationTopicPermission
import com.nimbusframework.nimbuscore.testing.notification.LocalNotificationTopic
import com.nimbusframework.nimbuscore.testing.services.LocalResourceHolder
import java.lang.reflect.Method

class LocalUsesNotificationTopicHandler(
        localResourceHolder: LocalResourceHolder,
        private val stage: String
): LocalUsesResourcesHandler(localResourceHolder) {

    override fun handleUsesResources(clazz: Class<out Any>, method: Method, functionEnvironment: FunctionEnvironment) {
        val usesNotificationTopics = method.getAnnotationsByType(UsesNotificationTopic::class.java)

        for (usesNotificationTopic in usesNotificationTopics) {
            if (usesNotificationTopic.stages.contains(stage)) {
                functionEnvironment.addPermission(PermissionType.NOTIFICATION_TOPIC, NotificationTopicPermission(usesNotificationTopic.topic))
            }
        }
    }
}