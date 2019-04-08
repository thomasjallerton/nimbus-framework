package com.nimbusframework.nimbuscore.wrappers.file.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class FileRecords (
        @JsonProperty(value = "Records")
        val records: List<FileRecord> = listOf()
)