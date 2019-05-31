package com.nimbusframework.nimbuscore.wrappers.queue.models

import com.amazonaws.services.lambda.runtime.events.SQSEvent
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.nimbusframework.nimbuscore.wrappers.ServerlessEvent
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
data class QueueEvent(
        val messageId: String? = null,
        val body: String? = null,
        val attributes: Map<String, String>? = mapOf(),
        val messageAttributes: Map<String, MessageAttribute>? = mapOf(),
        val md5OfMessageAttributes: String? = null,
        val md5OfBody: String? = null,
        val eventSource: String? = null,
        val requestId: String = UUID.randomUUID().toString()
) : ServerlessEvent {

    constructor(sqs: SQSEvent.SQSMessage, requestId: String): this (
            sqs.messageId,
            sqs.body,
            sqs.attributes,
            convertMap(sqs.messageAttributes),
            sqs.md5OfMessageAttributes,
            sqs.md5OfBody,
            sqs.eventSource,
            requestId
    )

    companion object {
        private fun convertMap(awsMap: Map<String, SQSEvent.MessageAttribute>): Map<String, MessageAttribute> {
            return awsMap.mapValues { (_, value) ->
                MessageAttribute(
                        value.stringValue,
                        value.stringListValues,
                        value.binaryListValues,
                        value.dataType,
                        value.binaryValue)
            }
        }
    }
}
