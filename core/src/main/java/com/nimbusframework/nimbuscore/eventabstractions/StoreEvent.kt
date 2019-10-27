package com.nimbusframework.nimbuscore.eventabstractions

import java.util.*

data class StoreEvent(
        val eventName: String? = "",
        val eventId: String? = "",
        val requestId: String = UUID.randomUUID().toString()
): ServerlessEvent