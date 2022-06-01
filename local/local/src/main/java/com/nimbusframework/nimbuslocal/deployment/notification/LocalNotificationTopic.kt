package com.nimbusframework.nimbuslocal.deployment.notification

import com.nimbusframework.nimbuscore.clients.JacksonClient
import com.nimbusframework.nimbuscore.clients.notification.Protocol
import java.util.*

class LocalNotificationTopic {

    val generalSubscribers: MutableMap<String, SubscriberInformation> = mutableMapOf()
    private val methodSubscribers: MutableList<NotificationMethod> = mutableListOf()
    private val notifiedEndpoints: MutableMap<SubscriberInformation, MutableList<Any>> = mutableMapOf()
    private var totalNotifications = 0

    internal fun addSubscriber(method: NotificationMethod) {
        methodSubscribers.add(method)
    }

    fun createSubscription(protocol: Protocol, endpoint: String): String {
        val id = UUID.randomUUID().toString()
        val subscriberInformation = SubscriberInformation(protocol, endpoint)
        generalSubscribers[id] = subscriberInformation
        notifiedEndpoints[subscriberInformation] = mutableListOf()
        return id
    }

    fun notify(message: String) {
        totalNotifications++
        generalSubscribers.forEach { subscriberInformation -> notifiedEndpoints[subscriberInformation.value]!!.add(message) }

        methodSubscribers.forEach { subscriber -> subscriber.invoke(message) }
    }

    internal fun notifyJson(json: Any) {
        totalNotifications++
        generalSubscribers.forEach { subscriberInformation -> notifiedEndpoints[subscriberInformation.value]!!.add(json) }

        methodSubscribers.forEach { subscriber -> subscriber.invoke(JacksonClient.writeValueAsString(json)) }
    }

    fun getTotalNumberOfNotifications(): Int {return totalNotifications}

    fun deleteSubscription(subscriptionId: String) {
        generalSubscribers.remove(subscriptionId)
    }

    fun getEndpointsMessages(protocol: Protocol, endpoint: String): List<Any> {
        val subscriberInformation = SubscriberInformation(protocol, endpoint)
        return if (notifiedEndpoints.contains(subscriberInformation)) {
            notifiedEndpoints[subscriberInformation]!!
        } else {
            listOf()
        }
    }

    fun getNumberOfSubscribers(): Int {
        return generalSubscribers.size + methodSubscribers.size
    }
}
