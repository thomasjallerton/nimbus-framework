package persisted

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class DeployedFunctionInformation(
        val mostRecentDeployedVersion: String = "",
        val mostRecentDeployedHash: String = ""
)