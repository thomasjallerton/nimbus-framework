package com.nimbusframework.nimbusaws.clients.queue

import com.nimbusframework.nimbusaws.annotation.annotations.parsed.ParsedQueueDefinition
import com.nimbusframework.nimbusaws.clients.InternalEnvironmentVariableClient
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.queue.QueueUrlEnvironmentVariable
import com.nimbusframework.nimbuscore.annotations.queue.QueueDefinition
import com.nimbusframework.nimbuscore.clients.JacksonClient
import software.amazon.awssdk.services.sqs.SqsClient
import com.nimbusframework.nimbuscore.clients.queue.QueueClient
import software.amazon.awssdk.services.sqs.model.SendMessageRequest

internal class QueueClientSQS(
    queueDefinition: ParsedQueueDefinition,
    private val sqsClient: SqsClient,
    private val internalEnvironmentVariableClient: InternalEnvironmentVariableClient
): QueueClient {

    private val queueUrl: String by lazy { internalEnvironmentVariableClient.get(QueueUrlEnvironmentVariable(queueDefinition)) ?: "" }

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
            .messageBody(JacksonClient.writeValueAsString(obj))
            .build()
        sqsClient.sendMessage(sndMessageRequest)
    }

}
