package wrappers.notification.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import wrappers.ServerlessEvent

@JsonIgnoreProperties(ignoreUnknown = true)
data class NotificationEvent(
        @JsonProperty(value = "Type")
        val type: String? = null,
        @JsonProperty(value = "MessageId")
        val messageId: String? = null,
        @JsonProperty(value = "Subject")
        val subject: String? = null,
        @JsonProperty(value = "Message")
        val message: String? = null,
        @JsonProperty(value = "Timestamp")
        val timestamp: String? = null,
        @JsonProperty(value = "MessageAttributes")
        val messageAttributes: Map<String, MessageAttribute>? = null
): ServerlessEvent