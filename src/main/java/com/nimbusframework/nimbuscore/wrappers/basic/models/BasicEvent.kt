package com.nimbusframework.nimbuscore.wrappers.basic.models

import com.nimbusframework.nimbuscore.wrappers.ServerlessEvent
import java.util.*

data class BasicEvent(
        val requestId: String = UUID.randomUUID().toString()
): ServerlessEvent