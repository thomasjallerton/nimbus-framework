package com.nimbusframework.nimbuslocal.eventabstractions

import com.nimbusframework.nimbuscore.eventabstractions.QueueEvent
import com.nimbusframework.nimbuscore.eventabstractions.StoreMessageAttribute
import java.util.*

class QueueEventBuilder {

    private var messageId: String? = null
    private var body: String? = null
    private var attributes: MutableMap<String, String> = mutableMapOf()
    private var messageAttributes: MutableMap<String, StoreMessageAttribute> = mutableMapOf()
    private var md5OfMessageAttributes: String? = null
    private var md5OfBody: String? = null
    private var eventSource: String? = null
    private var requestId: String = UUID.randomUUID().toString()

    fun withMessageId(messageId: String): QueueEventBuilder {
        this.messageId = messageId
        return this
    }

    fun withBody(body: String): QueueEventBuilder {
        this.body = body
        return this
    }

    fun withAttributes(attributes: MutableMap<String, String>): QueueEventBuilder {
        this.attributes = attributes
        return this
    }

    fun withAttribute(attribute: String, value: String): QueueEventBuilder {
        attributes[attribute] = value
        return this
    }

    fun withMessageAttributes(messageAttributes: MutableMap<String, StoreMessageAttribute>): QueueEventBuilder {
        this.messageAttributes = messageAttributes
        return this
    }

    fun withMessageAttribute(messageAttribute: String, value: StoreMessageAttribute): QueueEventBuilder {
        messageAttributes[messageAttribute] = value
        return this
    }

    fun withMd5OfMessageAttributes(md5OfMessageAttributes: String): QueueEventBuilder {
        this.md5OfMessageAttributes = md5OfMessageAttributes
        return this
    }

    fun withMd5OfBody(md5OfBody: String): QueueEventBuilder {
        this.md5OfBody = md5OfBody
        return this
    }

    fun withEventSource(eventSource: String): QueueEventBuilder {
        this.eventSource
        return this
    }

    fun withRequestId(requestId: String): QueueEventBuilder {
        this.requestId = requestId
        return this
    }

    fun build(): QueueEvent {
        return QueueEvent(
            messageId,
            body,
            attributes,
            messageAttributes,
            md5OfMessageAttributes,
            md5OfBody,
            eventSource,
            requestId
        )
    }

}