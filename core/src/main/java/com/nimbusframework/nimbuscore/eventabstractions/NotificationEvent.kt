package com.nimbusframework.nimbuscore.eventabstractions

import java.time.Instant
import java.util.*

data class NotificationEvent(
        val type: String? = null,
        val messageId: String? = null,
        val subject: String? = null,
        val message: String? = null,
        val timestamp: Instant? = Instant.now(),
        val messageAttributes: Map<String, NotificationMessageAttribute>? = null,
        val requestId: String = UUID.randomUUID().toString()
) : ServerlessEvent
