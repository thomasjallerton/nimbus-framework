package clients.queue

interface QueueClient {
    fun sendMessage(obj: Any)
}