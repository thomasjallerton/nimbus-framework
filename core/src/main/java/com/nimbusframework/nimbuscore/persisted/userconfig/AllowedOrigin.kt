package com.nimbusframework.nimbuscore.persisted.userconfig

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class AllowedOrigin(
    val stage: String = "",
    val allowedOrigin: String = ""
)
