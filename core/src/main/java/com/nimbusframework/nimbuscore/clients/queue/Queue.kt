package com.nimbusframework.nimbuscore.clients.queue

import com.nimbusframework.nimbuscore.clients.ClientBuilder

open class Queue(queueClass: Class<*>): QueueClient {

    private val queueClient = ClientBuilder.getQueueClient(queueClass)

    override fun sendMessage(message: String) {
        queueClient.sendMessage(message)
    }

    override fun sendMessageAsJson(obj: Any) {
        queueClient.sendMessageAsJson(obj)
    }

}