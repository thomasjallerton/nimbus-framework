package com.nimbusframework.nimbuscore.wrappers.notification.models

import com.fasterxml.jackson.annotation.JsonProperty

class MessageAttribute(
        @JsonProperty(value = "Type")
        val type: String? = null,
        @JsonProperty(value = "Value")
        val value: String? = null
)