package wrappers.store.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import wrappers.ServerlessEvent

@JsonIgnoreProperties(ignoreUnknown = true)
data class StoreEvent(
        @JsonProperty(value = "storeUpdateDetails")
        val storeUpdateDetails: StoreUpdateDetails? = null,
        val eventName: String = ""
): ServerlessEvent