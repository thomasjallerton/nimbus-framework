package com.nimbusframework.nimbusaws.clients.queue

import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.Inject
import com.nimbusframework.nimbuscore.clients.function.EnvironmentVariableClient
import com.nimbusframework.nimbuscore.clients.queue.QueueClient

internal class QueueClientSQS(id: String): QueueClient {

    @Inject
    private lateinit var sqsClient: AmazonSQS

    @Inject
    private lateinit var environmentVariableClient: EnvironmentVariableClient

    private val objectMapper = ObjectMapper()

    private val queueUrl: String by lazy { environmentVariableClient.get("NIMBUS_QUEUE_URL_ID_${id.toUpperCase()}") ?: "" }

    override fun sendMessage(message: String) {
        if (queueUrl == "") throw InvalidQueueUrlException()
        sqsClient.sendMessage(queueUrl, message)
    }

    override fun sendMessageAsJson(obj: Any) {
        if (queueUrl == "") throw InvalidQueueUrlException()
        sqsClient.sendMessage(queueUrl, objectMapper.writeValueAsString(obj))
    }

}