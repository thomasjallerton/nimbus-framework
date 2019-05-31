package com.nimbusframework.nimbuscore.wrappers.store.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.nimbusframework.nimbuscore.wrappers.ServerlessEvent

@JsonIgnoreProperties(ignoreUnknown = true)
data class StoreEvent(
        @JsonProperty(value = "dynamodb")
        val storeUpdateDetails: StoreUpdateDetails? = null,
        @JsonProperty(value = "eventName")
        val eventName: String = ""
): ServerlessEvent