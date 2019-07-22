package com.nimbusframework.nimbuscore.testing.notification

import com.nimbusframework.nimbuscore.clients.notification.Protocol
import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusframework.nimbuscore.testing.webserver.webconsole.models.FunctionSubscriberInformation
import java.util.*

class LocalNotificationTopic {

    val generalSubscribers: MutableMap<String, SubscriberInformation> = mutableMapOf()
    private val methodSubscribers: MutableList<NotificationMethod> = mutableListOf()
    private val notifiedEndpoints: MutableMap<SubscriberInformation, MutableList<Any>> = mutableMapOf()
    private var totalNotifications = 0

    internal fun addSubscriber(method: NotificationMethod) {
        methodSubscribers.add(method)
    }

    fun getFunctionSubscribers(): List<FunctionSubscriberInformation> {
        return methodSubscribers.map { it.getFunctionSubscriber() }
    }

    fun createSubscription(protocol: Protocol, endpoint: String): String {
        val id = UUID.randomUUID().toString()
        val subscriberInformation = SubscriberInformation(protocol, endpoint)
        generalSubscribers[id] = subscriberInformation
        notifiedEndpoints[subscriberInformation] = mutableListOf()
        return id
    }

    fun notify(message: Any) {
        totalNotifications++
        generalSubscribers.forEach { subscriberInformation -> notifiedEndpoints[subscriberInformation.value]!!.add(message) }

        val objectMapper = ObjectMapper()
        methodSubscribers.forEach { subscriber -> subscriber.invoke(objectMapper.writeValueAsString(message)) }
    }

    internal fun notifyJson(json: String) {
        totalNotifications++
        generalSubscribers.forEach { subscriberInformation -> notifiedEndpoints[subscriberInformation.value]!!.add(json) }

        methodSubscribers.forEach { subscriber -> subscriber.invoke(json) }
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