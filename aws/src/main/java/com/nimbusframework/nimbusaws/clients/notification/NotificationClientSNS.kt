package com.nimbusframework.nimbusaws.clients.notification

import com.nimbusframework.nimbuscore.clients.JacksonClient
import com.nimbusframework.nimbuscore.clients.function.EnvironmentVariableClient
import com.nimbusframework.nimbuscore.clients.notification.NotificationClient
import com.nimbusframework.nimbuscore.clients.notification.Protocol
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sns.model.PublishRequest
import software.amazon.awssdk.services.sns.model.SubscribeRequest
import software.amazon.awssdk.services.sns.model.UnsubscribeRequest

internal class NotificationClientSNS(
    topicName: String,
    private val snsClient: SnsClient,
    private val environmentVariableClient: EnvironmentVariableClient

): NotificationClient {

    private val topicArn: String by lazy {  environmentVariableClient.get("SNS_TOPIC_ARN_${topicName.toUpperCase()}") ?: "" }

    override fun createSubscription(protocol: Protocol, endpoint: String): String {
        val subscribeRequest = SubscribeRequest.builder()
            .topicArn(topicArn)
            .protocol(protocol.name)
            .endpoint(endpoint)
            .build()
        val result = snsClient.subscribe(subscribeRequest)
        return result.subscriptionArn()
    }

    override fun notify(message: String) {
        val publishRequest = PublishRequest.builder()
            .topicArn(topicArn)
            .message(message)
            .build()

        snsClient.publish(publishRequest)
    }

    override fun notifyJson(message: Any) {
        val publishRequest = PublishRequest.builder()
            .topicArn(topicArn)
            .message(JacksonClient.writeValueAsString(message))
            .build()

        snsClient.publish(publishRequest)
    }

    override fun deleteSubscription(subscriptionId: String) {
        val unsubscribeRequest = UnsubscribeRequest.builder()
            .subscriptionArn(subscriptionId)
            .build()
        snsClient.unsubscribe(unsubscribeRequest)
    }
}
