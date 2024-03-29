package com.nimbusframework.nimbusaws.wrappers.notification

import com.amazonaws.services.lambda.runtime.events.SNSEvent
import com.nimbusframework.nimbuscore.eventabstractions.NotificationEvent
import com.nimbusframework.nimbuscore.eventabstractions.NotificationMessageAttribute
import java.time.Instant

object SnsEventMapper {

    @JvmStatic
    fun getNotificationEvent(sns: SNSEvent.SNS, requestId: String): NotificationEvent {
        return NotificationEvent(sns.type,
                sns.messageId,
                sns.subject,
                sns.message,
                Instant.ofEpochMilli(sns.timestamp.millis),
                convertMap(sns.messageAttributes),
                requestId)
    }

    private fun convertMap(awsMap: Map<String, SNSEvent.MessageAttribute>): Map<String, NotificationMessageAttribute> {
        return awsMap.mapValues { (_, value) -> NotificationMessageAttribute(value.type, value.value) }
    }
}
