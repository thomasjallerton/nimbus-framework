package com.nimbusframework.nimbuscore.eventabstractions

import java.util.*

data class FileStorageEvent @JvmOverloads constructor(
    val key: String?,
    val size: Long?,
    val requestId: String = UUID.randomUUID().toString()
): ServerlessEvent