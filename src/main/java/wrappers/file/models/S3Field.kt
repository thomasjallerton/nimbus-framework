package wrappers.file.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class S3Field (
    @JsonProperty(value = "object")
    val obj: FileStorageEvent = FileStorageEvent()
)