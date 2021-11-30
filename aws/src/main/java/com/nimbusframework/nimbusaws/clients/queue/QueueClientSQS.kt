package com.nimbusframework.nimbusaws.clients.queue

import software.amazon.awssdk.services.sqs.SqsClient;
import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusframework.nimbuscore.clients.function.EnvironmentVariableClient
import com.nimbusframework.nimbuscore.clients.queue.QueueClient
import software.amazon.awssdk.services.sqs.model.SendMessageRequest

internal class QueueClientSQS(
    id: String,
    private val sqsClient: SqsClient,
    private val environmentVariableClient: EnvironmentVariableClient
): QueueClient {

    private val objectMapper = ObjectMapper()

    private val queueUrl: String by lazy { environmentVariableClient.get("NIMBUS_QUEUE_URL_ID_${id.toUpperCase()}") ?: "" }

    override fun sendMessage(message: String) {
        if (queueUrl == "") throw InvalidQueueUrlException()
        val sndMessageRequest = SendMessageRequest.builder()
            .queueUrl(queueUrl)
            .messageBody(message)
            .build()
        sqsClient.sendMessage(sndMessageRequest)
    }

    override fun sendMessageAsJson(obj: Any) {
        if (queueUrl == "") throw InvalidQueueUrlException()
        val sndMessageRequest = SendMessageRequest.builder()
            .queueUrl(queueUrl)
            .messageBody(objectMapper.writeValueAsString(obj))
            .build()
        sqsClient.sendMessage(sndMessageRequest)
    }

}
