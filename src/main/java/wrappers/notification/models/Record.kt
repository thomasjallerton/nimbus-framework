package wrappers.notification.models

import com.fasterxml.jackson.annotation.JsonProperty

data class Record(
        @JsonProperty(value = "EventSource")
        val eventSource: String? = null,
        @JsonProperty(value = "EventVersion")
        val eventVersion: String? = null,
        @JsonProperty(value = "EventSubscriptionArn")
        val eventSubscriptionArn: String? = null,
        @JsonProperty(value = "Sns")
        val sns: NotificationEvent? = null
)
