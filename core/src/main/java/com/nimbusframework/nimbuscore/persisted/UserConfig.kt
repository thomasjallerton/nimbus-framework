package com.nimbusframework.nimbuscore.persisted

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.nimbusframework.nimbuscore.annotations.NimbusConstants


@JsonIgnoreProperties(ignoreUnknown = true)
data class UserConfig(
        val projectName: String = "nimbus-project",
        val assemble: Boolean = false,
        val defaultStages: List<String> = listOf(NimbusConstants.stage),
        val keepWarmStages: List<String> = listOf()
)