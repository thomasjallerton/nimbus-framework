package com.nimbusframework.nimbusaws.wrappers.notification

import com.amazonaws.services.lambda.runtime.events.SNSEvent
import io.kotlintest.shouldBe
import io.kotlintest.specs.AnnotationSpec
import org.joda.time.DateTime

class SnsEventMapperTest: AnnotationSpec() {

    val underTest = SnsEventMapper

    @Test
    fun canParseSnsEvent() {
        val messageAttribute = SNSEvent.MessageAttribute()
                .withType("stringValue")
                .withValue("strVal")

        val attributes = mapOf(Pair("Key", messageAttribute))
        val time = DateTime.now()
        val requestEvent = SNSEvent.SNS()
                .withType("type")
                .withMessageId("messageId")
                .withSubject("subject")
                .withMessage("message")
                .withTimestamp(time)
                .withMessageAttributes(attributes)

        val result = underTest.getNotificationEvent(requestEvent, "requestId")

        result.type shouldBe "type"
        result.messageId shouldBe "messageId"
        result.subject shouldBe "subject"
        result.message shouldBe "message"
        result.timestamp shouldBe time
        result.requestId shouldBe "requestId"
        result.messageAttributes!!.getValue("Key").type shouldBe "stringValue"
        result.messageAttributes!!.getValue("Key").value shouldBe "strVal"

    }

}