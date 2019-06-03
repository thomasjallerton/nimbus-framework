package com.nimbusframework.nimbuscore.testing.notification

import com.nimbusframework.nimbuscore.clients.notification.Protocol
import com.fasterxml.jackson.databind.ObjectMapper
import java.util.*

class LocalNotificationTopic {

    private val generalSubscribers: MutableMap<String, SubscriberInformation> = mutableMapOf()
    private val methodSubscribers: MutableList<NotificationMethod> = mutableListOf()
    private val notifiedEndpoints: MutableMap<SubscriberInformation, MutableList<Any>> = mutableMapOf()

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

    fun notify(message: Any) {
        generalSubscribers.forEach { subscriberInformation -> notifiedEndpoints[subscriberInformation.value]!!.add(message) }

        val objectMapper = ObjectMapper()
        methodSubscribers.forEach { subscriber -> subscriber.invoke(objectMapper.writeValueAsString(message)) }
    }

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

    private data class SubscriberInformation(val protocol: Protocol, val endpoint: String)
}