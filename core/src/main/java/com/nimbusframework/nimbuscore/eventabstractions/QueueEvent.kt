package com.nimbusframework.nimbuscore.eventabstractions

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
data class QueueEvent(
        val messageId: String? = null,
        val body: String? = null,
        val attributes: Map<String, String>? = mapOf(),
        val messageAttributes: Map<String, StoreMessageAttribute>? = mapOf(),
        val md5OfMessageAttributes: String? = null,
        val md5OfBody: String? = null,
        val eventSource: String? = null,
        val requestId: String = UUID.randomUUID().toString()
) : ServerlessEvent