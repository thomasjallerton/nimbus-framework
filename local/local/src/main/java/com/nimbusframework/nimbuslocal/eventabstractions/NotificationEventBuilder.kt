package com.nimbusframework.nimbuslocal.eventabstractions

import com.nimbusframework.nimbuscore.eventabstractions.NotificationEvent
import com.nimbusframework.nimbuscore.eventabstractions.NotificationMessageAttribute
import java.time.Instant
import java.util.*

class NotificationEventBuilder {

    private var type: String? = null
    private var messageId: String? = null
    private var subject: String? = null
    private var message: String? = null
    private var timestamp: Instant = Instant.now()
    private var messageAttributes: MutableMap<String, NotificationMessageAttribute> = mutableMapOf()
    private var requestId: String = UUID.randomUUID().toString()

    fun withType(type: String): NotificationEventBuilder {
        this.type = type
        return this
    }

    fun withMessageId(messageId: String): NotificationEventBuilder {
        this.messageId = messageId
        return this
    }

    fun withSubject(subject: String): NotificationEventBuilder {
        this.subject = subject
        return this
    }

    fun withMessage(message: String): NotificationEventBuilder {
        this.message = message
        return this
    }

    fun withTimestamp(timestamp: Instant): NotificationEventBuilder {
        this.timestamp = timestamp
        return this
    }

    fun withMessageAttributes(messageAttributes: MutableMap<String, NotificationMessageAttribute>): NotificationEventBuilder {
        this.messageAttributes = messageAttributes
        return this
    }

    fun withMessageAttribute(attributeName: String, attribute: NotificationMessageAttribute): NotificationEventBuilder {
        messageAttributes[attributeName] = attribute
        return this
    }

    fun withRequestId(requestId: String): NotificationEventBuilder {
        this.requestId = requestId
        return this
    }

    fun build(): NotificationEvent {
        return NotificationEvent(
            type,
            messageId,
            subject,
            message,
            timestamp,
            messageAttributes,
            requestId
        )
    }
}
