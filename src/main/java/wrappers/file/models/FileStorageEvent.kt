package wrappers.file.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import wrappers.ServerlessEvent

@JsonIgnoreProperties(ignoreUnknown = true)
data class FileStorageEvent(
    val key: String = "",
    val size: Long = 0
): ServerlessEvent