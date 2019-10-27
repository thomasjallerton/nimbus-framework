package com.nimbusframework.nimbuscore.eventabstractions

import com.nimbusframework.nimbuscore.eventabstractions.ServerlessEvent
import java.util.*

data class BasicEvent(
        val requestId: String = UUID.randomUUID().toString()
): ServerlessEvent