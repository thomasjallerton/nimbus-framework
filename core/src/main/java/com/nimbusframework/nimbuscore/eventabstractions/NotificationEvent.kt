package com.nimbusframework.nimbuscore.eventabstractions

import org.joda.time.DateTime
import java.util.*

data class NotificationEvent(
        val type: String? = null,
        val messageId: String? = null,
        val subject: String? = null,
        val message: String? = null,
        val timestamp: DateTime? = DateTime.now(),
        val messageAttributes: Map<String, NotificationMessageAttribute>? = null,
        val requestId: String = UUID.randomUUID().toString()
) : ServerlessEvent