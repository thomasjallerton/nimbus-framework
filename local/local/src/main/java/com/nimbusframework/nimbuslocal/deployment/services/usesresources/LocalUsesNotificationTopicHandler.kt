package com.nimbusframework.nimbuslocal.deployment.services.usesresources

import com.nimbusframework.nimbuscore.annotations.notification.UsesNotificationTopic
import com.nimbusframework.nimbuscore.clients.notification.NotificationTopicAnnotationService
import com.nimbusframework.nimbuscore.permissions.PermissionType
import com.nimbusframework.nimbuslocal.deployment.function.FunctionEnvironment
import com.nimbusframework.nimbuslocal.deployment.function.permissions.NotificationTopicPermission
import com.nimbusframework.nimbuslocal.deployment.services.LocalResourceHolder
import com.nimbusframework.nimbuslocal.deployment.services.StageService
import java.lang.reflect.Method

class LocalUsesNotificationTopicHandler(
        localResourceHolder: LocalResourceHolder,
        private val stageService: StageService
): LocalUsesResourcesHandler(localResourceHolder) {

    override fun handleUsesResources(clazz: Class<out Any>, method: Method, functionEnvironment: FunctionEnvironment) {
        val usesNotificationTopics = method.getAnnotationsByType(UsesNotificationTopic::class.java)

        val annotation = stageService.annotationForStage(usesNotificationTopics) { annotation -> annotation.stages}
        if (annotation != null) {
            val topicName = NotificationTopicAnnotationService.getTopicName(annotation.notificationTopic.java, stageService.deployingStage)
            functionEnvironment.addPermission(PermissionType.NOTIFICATION_TOPIC, NotificationTopicPermission(topicName))
        }
    }
}