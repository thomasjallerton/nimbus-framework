package com.nimbusframework.nimbuscore.wrappers.queue.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.nimbusframework.nimbuscore.wrappers.ServerlessEvent

@JsonIgnoreProperties(ignoreUnknown = true)
data class QueueEvent(
        val messageId: String? = null,
        val receiptHandle: String? = null,
        val body: String? = null,
        val attributes: Attributes? = null,
        val messageAttributes: Map<String, MessageAttribute>? = null,
        val md5OfMessageAttributes: String? = null,
        val md5OfBody: String? = null,
        val eventSource: String? = null
): ServerlessEvent
