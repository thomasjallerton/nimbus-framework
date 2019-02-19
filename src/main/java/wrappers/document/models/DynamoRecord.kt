package wrappers.document.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class DynamoRecord(
        val dynamodb: DynamoUpdate = DynamoUpdate(),
        val eventName: String = ""
)