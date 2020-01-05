package com.nimbusframework.nimbuscore.clients.notification

import com.nimbusframework.nimbuscore.clients.ClientBuilder

class NotificationTopic(notificationTopicClass: Class<*>): NotificationClient {

    private val notificationClient = ClientBuilder.getNotificationClient(notificationTopicClass)

    override fun createSubscription(protocol: Protocol, endpoint: String): String {
        return notificationClient.createSubscription(protocol, endpoint)
    }

    override fun notify(message: String) {
        notificationClient.notify(message)
    }

    override fun notifyJson(message: Any) {
        notificationClient.notifyJson(message)
    }

    override fun deleteSubscription(subscriptionId: String) {
        notificationClient.deleteSubscription(subscriptionId)
    }


}