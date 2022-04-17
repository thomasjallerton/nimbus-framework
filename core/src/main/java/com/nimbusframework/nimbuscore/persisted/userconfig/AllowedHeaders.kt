package com.nimbusframework.nimbuscore.persisted.userconfig

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class AllowedHeaders(
    val stage: String = "",
    val allowedHeaders: List<String> = listOf()
)
