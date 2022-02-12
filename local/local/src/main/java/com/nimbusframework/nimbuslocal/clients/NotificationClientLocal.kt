package com.nimbusframework.nimbuslocal.clients

import com.nimbusframework.nimbuscore.clients.notification.NotificationClient
import com.nimbusframework.nimbuscore.clients.notification.NotificationTopicAnnotationService
import com.nimbusframework.nimbuscore.clients.notification.Protocol
import com.nimbusframework.nimbuscore.permissions.PermissionType

internal class NotificationClientLocal(topicClass: Class<*>, stage: String): NotificationClient, LocalClient() {

    private val topic = NotificationTopicAnnotationService.getTopicName(topicClass, stage)
    private val notificationTopic = localNimbusDeployment.getNotificationTopic(topicClass)

    override fun canUse(): Boolean {
        return checkPermissions(PermissionType.NOTIFICATION_TOPIC, topic)
    }

    override val clientName: String = NotificationClient::class.java.simpleName

    override fun createSubscription(protocol: Protocol, endpoint: String): String {
        checkClientUse()
        return notificationTopic.createSubscription(protocol, endpoint)
    }

    override fun notify(message: String) {
        checkClientUse()
        notificationTopic.notify(message)
    }

    override fun notifyJson(message: Any) {
        checkClientUse()
        notificationTopic.notifyJson(message)
    }

    override fun deleteSubscription(subscriptionId: String) {
        checkClientUse()
        notificationTopic.deleteSubscription(subscriptionId)
    }

}