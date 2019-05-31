package com.nimbusframework.nimbuscore.wrappers.notification.models

import com.amazonaws.services.lambda.runtime.events.SNSEvent
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.nimbusframework.nimbuscore.wrappers.ServerlessEvent
import org.joda.time.DateTime
import java.util.*

data class NotificationEvent(
        val type: String? = null,
        val messageId: String? = null,
        val subject: String? = null,
        val message: String? = null,
        val timestamp: DateTime? = DateTime.now(),
        val messageAttributes: Map<String, MessageAttribute>? = null,
        val requestId: String = UUID.randomUUID().toString()
) : ServerlessEvent {

    constructor(sns: SNSEvent.SNS, requestId: String) : this(
            sns.type,
            sns.messageId,
            sns.subject,
            sns.message,
            sns.timestamp,
            convertMap(sns.messageAttributes),
            requestId
    )

    companion object {
        private fun convertMap(awsMap: Map<String, SNSEvent.MessageAttribute>): Map<String, MessageAttribute> {
            return awsMap.mapValues { (_, value) -> MessageAttribute(value.type, value.value) }
        }
    }
}