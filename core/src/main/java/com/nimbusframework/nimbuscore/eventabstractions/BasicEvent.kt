package com.nimbusframework.nimbuscore.eventabstractions

import java.util.*

data class BasicEvent(
        val requestId: String = UUID.randomUUID().toString()
): ServerlessEvent