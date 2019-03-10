package persisted

data class NimbusState(
        val projectName: String,
        val compilationTimeStamp: String,
        val afterDeployments: MutableMap<String, MutableList<String>> = mutableMapOf()
)