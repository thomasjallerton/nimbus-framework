package annotation.cloudformation.persisted

import com.fasterxml.jackson.annotation.JsonIgnoreProperties


@JsonIgnoreProperties(ignoreUnknown = true)
data class UserConfig(val projectName: String = "nimbus-project")