package com.nimbusframework.nimbuscore.wrappers.file.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.nimbusframework.nimbuscore.wrappers.ServerlessEvent

@JsonIgnoreProperties(ignoreUnknown = true)
data class FileStorageEvent(
    val key: String = "",
    val size: Long = 0
): ServerlessEvent