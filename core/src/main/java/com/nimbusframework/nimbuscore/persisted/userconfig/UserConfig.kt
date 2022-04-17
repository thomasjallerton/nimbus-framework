package com.nimbusframework.nimbuscore.persisted.userconfig

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.nimbusframework.nimbuscore.annotations.NimbusConstants


@JsonIgnoreProperties(ignoreUnknown = true)
data class UserConfig(
    val projectName: String = "nimbus-project",
    val customRuntime: Boolean = false,
    val defaultStages: List<String> = listOf(NimbusConstants.stage),
    val keepWarmStages: List<String> = listOf(),
    val defaultAllowedHeaders: List<AllowedHeaders> = listOf(),
    val defaultAllowedOrigin: List<AllowedOrigin> = listOf()
) {

    @JsonIgnore
    fun getAllowedHeaders(): Map<String, List<String>> {
        return defaultAllowedHeaders.associateBy { it.stage }.mapValues { it.value.allowedHeaders }
    }

    @JsonIgnore
    fun getAllowedOrigins(): Map<String, String> {
        return defaultAllowedOrigin.associateBy { it.stage }.mapValues { it.value.allowedOrigin }
    }

}
