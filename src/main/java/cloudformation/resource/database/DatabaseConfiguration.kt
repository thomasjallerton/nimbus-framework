package cloudformation.resource.database

import annotation.annotations.database.DatabaseLanguage
import annotation.annotations.database.DatabaseSize

data class DatabaseConfiguration(
        val name: String,
        val username: String,
        val password: String,
        val databaseLanguage: DatabaseLanguage,
        val databaseSize: DatabaseSize,
        val size: Int
)