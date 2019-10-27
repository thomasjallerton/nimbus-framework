package com.nimbusframework.nimbuscore.clients.notification

interface NotificationClient {

    fun createSubscription(protocol: Protocol, endpoint: String): String
    fun notify(message: String)
    fun notifyJson(message: Any)
    fun deleteSubscription(subscriptionId: String)
}