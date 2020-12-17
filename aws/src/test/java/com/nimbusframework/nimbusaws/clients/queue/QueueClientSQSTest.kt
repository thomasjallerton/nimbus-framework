package com.nimbusframework.nimbusaws.clients.queue

import com.amazonaws.services.sqs.AmazonSQS
import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.nimbusframework.nimbusaws.examples.SimpleObject
import com.nimbusframework.nimbuscore.clients.function.EnvironmentVariableClient
import io.kotest.core.spec.style.AnnotationSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class QueueClientSQSTest : AnnotationSpec() {

    private lateinit var underTest: QueueClientSQS
    private lateinit var sqsClient: AmazonSQS
    private lateinit var environmentVariableClient: EnvironmentVariableClient

    private val QUEUE_ID = "QUEUE"
    @BeforeEach
    fun setup() {
        underTest = QueueClientSQS(QUEUE_ID)
        sqsClient = mockk(relaxed = true)
        environmentVariableClient = mockk()
        val injector = Guice.createInjector(object: AbstractModule() {
            override fun configure() {
                bind(AmazonSQS::class.java).toInstance(sqsClient)
                bind(EnvironmentVariableClient::class.java).toInstance(environmentVariableClient)
            }
        })
        injector.injectMembers(underTest)
    }

    @Test
    fun canSendStringMessageWhenUrlSet() {
        every { environmentVariableClient.get("NIMBUS_QUEUE_URL_ID_QUEUE") } returns "URL"

        underTest.sendMessage("TEST MESSAGE")

        verify(exactly = 1) { sqsClient.sendMessage("URL", "TEST MESSAGE") }
    }

    @Test(InvalidQueueUrlException::class)
    fun sendStringThrowsExceptionWhenUrlNotSet() {
        every { environmentVariableClient.get("NIMBUS_QUEUE_URL_ID_QUEUE") } returns ""

        underTest.sendMessage("TEST MESSAGE")
    }

    @Test
    fun canSendJsonMessageWhenUrlSet() {
        every { environmentVariableClient.get("NIMBUS_QUEUE_URL_ID_QUEUE") } returns "URL"

        underTest.sendMessageAsJson(SimpleObject("test"))

        verify(exactly = 1) { sqsClient.sendMessage("URL", "{\"value\":\"test\"}") }
    }

    @Test(InvalidQueueUrlException::class)
    fun sendJsonMessageThrowsExceptionWhenUrlNotSet() {
        every { environmentVariableClient.get("NIMBUS_QUEUE_URL_ID_QUEUE") } returns ""

        underTest.sendMessageAsJson(SimpleObject("test"))
    }
}