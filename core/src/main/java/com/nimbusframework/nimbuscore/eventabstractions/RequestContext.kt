package com.nimbusframework.nimbuscore.eventabstractions

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class RequestContext(
    val eventType: String = "",
    val connectionId: String = ""
)