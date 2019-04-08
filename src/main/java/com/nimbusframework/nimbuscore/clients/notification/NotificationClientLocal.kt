package com.nimbusframework.nimbuscore.clients.notification

import com.nimbusframework.nimbuscore.testing.LocalNimbusDeployment

internal class NotificationClientLocal(topic: String): NotificationClient {

    private val localDeployment = LocalNimbusDeployment.getInstance()
    private val notificationTopic = localDeployment.getNotificationTopic(topic)

    override fun createSubscription(protocol: Protocol, endpoint: String): String {
        return notificationTopic.createSubscription(protocol, endpoint)
    }

    override fun notify(message: String) {
        notificationTopic.notify(message)
    }

    override fun notifyJson(message: Any) {
        notificationTopic.notify(message)
    }

    override fun deleteSubscription(subscriptionId: String) {
        notificationTopic.deleteSubscription(subscriptionId)
    }

}