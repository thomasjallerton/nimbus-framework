package com.nimbusframework.nimbusaws.wrappers.queue

import com.amazonaws.services.lambda.runtime.events.SQSEvent
import io.kotlintest.shouldBe
import io.kotlintest.specs.AnnotationSpec

class SqsEventMapperTest: AnnotationSpec() {

    val underTest = SqsEventMapper

    @Test
    fun canProcessSqsEvent() {
        val messageAttribute = SQSEvent.MessageAttribute()
        messageAttribute.stringValue = "strVal"
        messageAttribute.dataType = "stringValue"

        val attributes = mapOf(Pair("AttributeKey", "AttributeVal"))

        val messageAttributes = mapOf(Pair("Key", messageAttribute))
        val requestEvent = SQSEvent.SQSMessage()
        requestEvent.messageId = "messageId"
        requestEvent.body = "body"
        requestEvent.attributes = attributes
        requestEvent.messageAttributes = messageAttributes
        requestEvent.md5OfBody = "md5"
        requestEvent.md5OfMessageAttributes = "md5Attributes"
        requestEvent.eventSource = "source"

        val result = underTest.getQueueEvent(requestEvent, "requestId")

        result.messageId shouldBe "messageId"
        result.body shouldBe "body"
        result.attributes shouldBe attributes
        result.messageAttributes!!["Key"]!!.stringValue shouldBe "strVal"
        result.messageAttributes!!["Key"]!!.dataType shouldBe "stringValue"
        result.md5OfBody shouldBe "md5"
        result.md5OfMessageAttributes shouldBe "md5Attributes"
        requestEvent.eventSource shouldBe "source"
        result.eventSource shouldBe "source"
        result.requestId shouldBe "requestId"
    }
}