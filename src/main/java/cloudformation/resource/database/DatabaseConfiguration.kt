package cloudformation.resource.database

data class DatabaseConfiguration(
        val name: String,
        val username: String,
        val password: String
)