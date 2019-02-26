package clients.queue

import testing.LocalNimbusDeployment

internal class QueueClientLocal(private val id: String): QueueClient {

    private val localDeployment = LocalNimbusDeployment.getInstance()

    override fun sendMessage(obj: Any) {
        localDeployment.getQueue(id).add(obj)
    }
}