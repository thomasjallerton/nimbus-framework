package com.nimbusframework.nimbuscore.clients.notification

import com.nimbusframework.nimbuscore.clients.LocalClient
import com.nimbusframework.nimbuscore.testing.function.PermissionType

internal class NotificationClientLocal(private val topic: String): NotificationClient, LocalClient() {

    private val notificationTopic = localNimbusDeployment.getNotificationTopic(topic)

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
        notificationTopic.notify(message)
    }

    override fun deleteSubscription(subscriptionId: String) {
        checkClientUse()
        notificationTopic.deleteSubscription(subscriptionId)
    }

}