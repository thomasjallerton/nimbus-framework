package com.nimbusframework.nimbusaws.wrappers.queue

import com.amazonaws.services.lambda.runtime.events.SQSEvent
import com.nimbusframework.nimbuscore.eventabstractions.QueueEvent
import com.nimbusframework.nimbuscore.eventabstractions.StoreMessageAttribute

object SqsEventMapper {

    @JvmStatic
    fun getQueueEvent(sqs: SQSEvent.SQSMessage, requestId: String): QueueEvent {
        return QueueEvent(sqs.messageId,
                sqs.body,
                sqs.attributes,
                convertMap(sqs.messageAttributes),
                sqs.md5OfMessageAttributes,
                sqs.md5OfBody,
                sqs.eventSource,
                requestId)
    }

    private fun convertMap(awsMap: Map<String, SQSEvent.MessageAttribute>): Map<String, StoreMessageAttribute> {
        return awsMap.mapValues { (_, value) ->
            StoreMessageAttribute(
                    value.stringValue,
                    value.stringListValues,
                    value.binaryListValues,
                    value.dataType,
                    value.binaryValue)
        }
    }
}