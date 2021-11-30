package com.nimbusframework.nimbusaws.clients.notification

import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.nimbusframework.nimbusaws.examples.SimpleObject
import com.nimbusframework.nimbuscore.clients.function.EnvironmentVariableClient
import com.nimbusframework.nimbuscore.clients.notification.Protocol
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sns.model.PublishRequest
import software.amazon.awssdk.services.sns.model.SubscribeRequest
import software.amazon.awssdk.services.sns.model.SubscribeResponse
import software.amazon.awssdk.services.sns.model.UnsubscribeRequest

class NotificationClientSNSTest : AnnotationSpec() {

    private lateinit var underTest: NotificationClientSNS
    private lateinit var snsClient: SnsClient
    private lateinit var environmentVariableClient: EnvironmentVariableClient

    private val TOPIC_NAME = "TOPIC"
    @BeforeEach
    fun setup() {
        snsClient = mockk(relaxed = true)
        environmentVariableClient = mockk()
        every { environmentVariableClient.get("SNS_TOPIC_ARN_TOPIC") } returns "ARN"
        underTest = NotificationClientSNS(TOPIC_NAME, snsClient, environmentVariableClient)
    }

    @Test
    fun canCreateSubscription() {
        val subscribeRequest = slot<SubscribeRequest>()
        every { snsClient.subscribe(capture(subscribeRequest)) } returns SubscribeResponse.builder().subscriptionArn("SUBSCRIPTION_ARN").build()

        underTest.createSubscription(Protocol.SMS, "endpoint") shouldBe "SUBSCRIPTION_ARN"

        subscribeRequest.captured.topicArn() shouldBe "ARN"
        subscribeRequest.captured.protocol() shouldBe "SMS"
        subscribeRequest.captured.endpoint() shouldBe "endpoint"
    }

    @Test
    fun canNotifyString() {
        val publishRequest = slot<PublishRequest>()
        every { snsClient.publish(capture(publishRequest)) } returns mockk()

        underTest.notify("TEST MESSAGE")
        publishRequest.captured.topicArn() shouldBe "ARN"
        publishRequest.captured.message() shouldBe "TEST MESSAGE"
    }

    @Test
    fun canNotifyJson() {
        val publishRequest = slot<PublishRequest>()
        every { snsClient.publish(capture(publishRequest)) } returns mockk()

        underTest.notifyJson(SimpleObject("TEST"))
        publishRequest.captured.topicArn() shouldBe "ARN"
        publishRequest.captured.message() shouldBe "{\"value\":\"TEST\"}"
    }

    @Test
    fun canDeleteSubscription() {
        val unsubscribeRequest = slot<UnsubscribeRequest>()
        every { snsClient.unsubscribe(capture(unsubscribeRequest)) } returns mockk()

        underTest.deleteSubscription("SUBSCRIPTION")
        unsubscribeRequest.captured.subscriptionArn() shouldBe "SUBSCRIPTION"
    }

}
