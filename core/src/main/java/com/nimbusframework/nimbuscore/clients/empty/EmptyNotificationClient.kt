package com.nimbusframework.nimbuscore.clients.empty

import com.nimbusframework.nimbuscore.clients.notification.NotificationClient
import com.nimbusframework.nimbuscore.clients.notification.Protocol
import com.nimbusframework.nimbuscore.exceptions.PermissionException

class EmptyNotificationClient: NotificationClient {
    private val clientName = "NotificationClient"

    override fun createSubscription(protocol: Protocol, endpoint: String): String {
        throw PermissionException(clientName)
    }

    override fun notify(message: String) {
        throw PermissionException(clientName)
    }

    override fun notifyJson(message: Any) {
        throw PermissionException(clientName)
    }

    override fun deleteSubscription(subscriptionId: String) {
        throw PermissionException(clientName)
    }

}