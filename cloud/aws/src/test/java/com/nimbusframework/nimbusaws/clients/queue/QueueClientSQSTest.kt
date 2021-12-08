package com.nimbusframework.nimbusaws.clients.queue

import com.nimbusframework.nimbusaws.examples.SimpleObject
import com.nimbusframework.nimbuscore.clients.function.EnvironmentVariableClient
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.SendMessageRequest

class QueueClientSQSTest : AnnotationSpec() {

    private lateinit var underTest: QueueClientSQS
    private lateinit var sqsClient: SqsClient
    private lateinit var environmentVariableClient: EnvironmentVariableClient

    private val QUEUE_ID = "QUEUE"
    @BeforeEach
    fun setup() {
        sqsClient = mockk(relaxed = true)
        environmentVariableClient = mockk()
        underTest = QueueClientSQS(QUEUE_ID, sqsClient, environmentVariableClient)
    }

    @Test
    fun canSendStringMessageWhenUrlSet() {
        every { environmentVariableClient.get("NIMBUS_QUEUE_URL_ID_QUEUE") } returns "URL"

        val sendMessageRequest = slot<SendMessageRequest>()
        every { sqsClient.sendMessage(capture(sendMessageRequest)) } returns mockk()

        underTest.sendMessage("TEST MESSAGE")

        sendMessageRequest.captured.messageBody() shouldBe "TEST MESSAGE"
        sendMessageRequest.captured.queueUrl() shouldBe "URL"
    }

    @Test(InvalidQueueUrlException::class)
    fun sendStringThrowsExceptionWhenUrlNotSet() {
        every { environmentVariableClient.get("NIMBUS_QUEUE_URL_ID_QUEUE") } returns ""

        underTest.sendMessage("TEST MESSAGE")
    }

    @Test
    fun canSendJsonMessageWhenUrlSet() {
        every { environmentVariableClient.get("NIMBUS_QUEUE_URL_ID_QUEUE") } returns "URL"

        val sendMessageRequest = slot<SendMessageRequest>()
        every { sqsClient.sendMessage(capture(sendMessageRequest)) } returns mockk()

        underTest.sendMessageAsJson(SimpleObject("test"))

        sendMessageRequest.captured.messageBody() shouldBe "{\"value\":\"test\"}"
        sendMessageRequest.captured.queueUrl() shouldBe "URL"
    }

    @Test(InvalidQueueUrlException::class)
    fun sendJsonMessageThrowsExceptionWhenUrlNotSet() {
        every { environmentVariableClient.get("NIMBUS_QUEUE_URL_ID_QUEUE") } returns ""

        underTest.sendMessageAsJson(SimpleObject("test"))
    }
}
