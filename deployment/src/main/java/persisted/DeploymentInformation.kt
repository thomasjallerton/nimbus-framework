package persisted

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class DeploymentInformation(
        val mostRecentCompilationTimestamp: String = "",
        val mostRecentDeployedFunctions: MutableMap<String, DeployedFunctionInformation> = mutableMapOf()
)