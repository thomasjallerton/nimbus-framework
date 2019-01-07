package wrappers.notification.models

import com.fasterxml.jackson.annotation.JsonProperty
import wrappers.notification.models.MessageAttribute

data class NotificationEvent(
        @JsonProperty(value = "Type")
        val type: String? = null,
        @JsonProperty(value = "MessageId")
        val messageId: String? = null,
        @JsonProperty(value = "TopicArn")
        val topicArn: String? = null,
        @JsonProperty(value = "Subject")
        val subject: String? = null,
        @JsonProperty(value = "Message")
        val message: String? = null,
        @JsonProperty(value = "Timestamp")
        val timestamp: String? = null,
        @JsonProperty(value = "SignatureVersion")
        val signatureVersion: String? = null,
        @JsonProperty(value = "Signature")
        val signature: String? = null,
        @JsonProperty(value = "SigningCertUrl")
        val signingCertUrl: String? = null,
        @JsonProperty(value = "UnsubscribeUrl")
        val unsubscribeUrl: String? = null,
        @JsonProperty(value = "MessageAttributes")
        val messageAttributes: Map<String, MessageAttribute>? = null
)