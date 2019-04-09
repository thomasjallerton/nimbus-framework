package com.nimbusframework.nimbuscore.clients.queue

import com.nimbusframework.nimbuscore.clients.LocalClient
import com.nimbusframework.nimbuscore.testing.function.PermissionType

internal class QueueClientLocal(private val id: String): QueueClient, LocalClient() {

    override fun canUse(): Boolean {
        return checkPermissions(PermissionType.QUEUE, id)
    }

    override val clientName: String = QueueClient::class.java.simpleName

    private val queue = localNimbusDeployment.getQueue(id)

    override fun sendMessage(message: String) {
        checkClientUse()
        queue.add(message)
    }

    override fun sendMessageAsJson(obj: Any) {
        checkClientUse()
        queue.add(obj)
    }
}