package com.nimbusframework.nimbuscore.clients.queue

import com.nimbusframework.nimbuscore.clients.PermissionException

class EmptyQueueClient: QueueClient {
    override fun sendMessage(message: String) {
        throw PermissionException(clientName)
    }

    override fun sendMessageAsJson(obj: Any) {
        throw PermissionException(clientName)
    }

    private val clientName = "QueueClient"

}