package persisted

data class NimbusState(
        val projectName: String,
        val compilationTimeStamp: String,
        val afterDeployments: MutableList<String> = mutableListOf()
)