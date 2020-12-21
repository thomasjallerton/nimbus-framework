package com.nimbusframework.nimbusaws.clients.notification

import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.model.SubscribeResult
import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.nimbusframework.nimbusaws.examples.SimpleObject
import com.nimbusframework.nimbuscore.clients.function.EnvironmentVariableClient
import com.nimbusframework.nimbuscore.clients.notification.Protocol
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class NotificationClientSNSTest : AnnotationSpec() {

    private lateinit var underTest: NotificationClientSNS
    private lateinit var snsClient: AmazonSNS
    private lateinit var environmentVariableClient: EnvironmentVariableClient

    private val TOPIC_NAME = "TOPIC"
    @BeforeEach
    fun setup() {
        underTest = NotificationClientSNS(TOPIC_NAME)
        snsClient = mockk(relaxed = true)
        environmentVariableClient = mockk()
        val injector = Guice.createInjector(object: AbstractModule() {
            override fun configure() {
                bind(AmazonSNS::class.java).toInstance(snsClient)
                bind(EnvironmentVariableClient::class.java).toInstance(environmentVariableClient)
            }
        })
        injector.injectMembers(underTest)
        every { environmentVariableClient.get("SNS_TOPIC_ARN_TOPIC") } returns "ARN"
    }

    @Test
    fun canCreateSubscription() {
        every { snsClient.subscribe("ARN", "SMS", "endpoint") } returns SubscribeResult().withSubscriptionArn("SUBSCRIPTION_ARN")

        underTest.createSubscription(Protocol.SMS, "endpoint") shouldBe "SUBSCRIPTION_ARN"
    }

    @Test
    fun canNotifyString() {
        underTest.notify("TEST MESSAGE")
        verify(exactly = 1) { snsClient.publish("ARN", "TEST MESSAGE") }
    }

    @Test
    fun canNotifyJson() {
        underTest.notifyJson(SimpleObject("TEST"))
        verify(exactly = 1) { snsClient.publish("ARN", "{\"value\":\"TEST\"}") }
    }

    @Test
    fun canDeleteSubscription() {
        underTest.deleteSubscription("SUBSCRIPTION")
        verify(exactly = 1) { snsClient.unsubscribe("SUBSCRIPTION") }
    }

}