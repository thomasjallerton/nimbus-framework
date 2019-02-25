package clients.notification

import com.amazonaws.services.sns.AmazonSNSClientBuilder
import com.fasterxml.jackson.databind.ObjectMapper

internal class NotificationClientSNS(topicName: String): NotificationClient {

    private val snsClient = AmazonSNSClientBuilder.defaultClient()
    private val topicArn: String = System.getenv("SNS_TOPIC_ARN_${topicName.toUpperCase()}")
    private val objectMapper: ObjectMapper = ObjectMapper()

    init {
        if (!System.getenv().containsKey("SNS_TOPIC_ARN_${topicName.toUpperCase()}")) {
            throw InvalidTopicException()
        }
    }

    override fun createSubscription(protocol: Protocol, endpoint: String): String {
        val result = snsClient.subscribe(topicArn, protocol.name, endpoint)
        return result.subscriptionArn
    }

    override fun notify(message: String) {
        snsClient.publish(topicArn, message)
    }

    override fun notifyJson(message: Any) {
        snsClient.publish(topicArn, objectMapper.writeValueAsString(message))
    }

    override fun deleteSubscription(subscriptionId: String) {
        snsClient.unsubscribe(subscriptionId)
    }
}