package com.nimbusframework.nimbuscore.clients.empty

import com.nimbusframework.nimbuscore.clients.queue.QueueClient
import com.nimbusframework.nimbuscore.exceptions.PermissionException

class EmptyQueueClient: QueueClient {
    override fun sendMessage(message: String) {
        throw PermissionException(clientName)
    }

    override fun sendMessageAsJson(obj: Any) {
        throw PermissionException(clientName)
    }

    private val clientName = "QueueClient"

}