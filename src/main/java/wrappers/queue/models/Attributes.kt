package wrappers.queue.models

import com.fasterxml.jackson.annotation.JsonProperty

data class Attributes(
        @JsonProperty(value = "ApproximateReceiveCount")
        val approximateReceiveCount: String? = null,
        @JsonProperty(value = "SentTimestamp")
        val sentTimestamp: String? = null,
        @JsonProperty(value = "SenderId")
        val senderId: String? = null,
        @JsonProperty(value = "ApproximateFirstReceiveTimestamp")
        val approximateFirstReceiveTimestamp: String? = null
)