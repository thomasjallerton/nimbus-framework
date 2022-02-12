package com.nimbusframework.nimbuscore.eventabstractions

data class RequestContext(
    val eventType: String = "",
    val connectionId: String = ""
)
