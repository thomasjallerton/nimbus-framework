package com.nimbusframework.nimbuslocal.clients

import com.nimbusframework.nimbuscore.clients.queue.QueueClient
import com.nimbusframework.nimbuscore.clients.queue.QueueIdAnnotationService
import com.nimbusframework.nimbuscore.permissions.PermissionType

internal class QueueClientLocal(queueClass: Class<*>, stage: String): QueueClient, LocalClient() {

    private val queueId = QueueIdAnnotationService.getQueueId(queueClass, stage)

    override fun canUse(): Boolean {
        return checkPermissions(PermissionType.QUEUE, queueId)
    }

    override val clientName: String = QueueClient::class.java.simpleName

    private val queue = localNimbusDeployment.getQueue(queueClass)

    override fun sendMessage(message: String) {
        checkClientUse()
        queue.add(message)
    }

    override fun sendMessageAsJson(obj: Any) {
        checkClientUse()
        queue.add(obj)
    }
}