package clients.queue

import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import com.fasterxml.jackson.databind.ObjectMapper

internal class QueueClientSQS(id: String): QueueClient {

    private val sqsClient = AmazonSQSClientBuilder.defaultClient()
    private val objectMapper = ObjectMapper()
    private val queueUrl: String = System.getenv("NIMBUS_QUEUE_URL_ID_${id.toUpperCase()}") ?: ""

    override fun sendMessage(obj: Any) {
        if (queueUrl == "") throw InvalidQueueUrlException()
        sqsClient.sendMessage(queueUrl, objectMapper.writeValueAsString(obj))
    }

}