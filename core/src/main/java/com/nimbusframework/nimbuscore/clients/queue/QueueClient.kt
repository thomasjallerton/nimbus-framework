package com.nimbusframework.nimbuscore.clients.queue

interface QueueClient {
    fun sendMessage(message: String)
    fun sendMessageAsJson(obj: Any)
}