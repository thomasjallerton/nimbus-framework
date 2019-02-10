package clients.queue

import testing.LocalNimbusDeployment

class QueueClientLocal(private val id: String): QueueClient {

    private val localDeployment = LocalNimbusDeployment.getInstance()

    override fun sendMessage(obj: Any) {
        localDeployment.getQueue(id).add(obj)
    }
}