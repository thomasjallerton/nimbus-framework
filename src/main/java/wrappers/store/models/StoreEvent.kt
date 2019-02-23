package wrappers.store.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import wrappers.ServerlessEvent

@JsonIgnoreProperties(ignoreUnknown = true)
data class StoreEvent(
        val dynamodb: DynamoUpdate? = null,
        val eventName: String = ""
): ServerlessEvent