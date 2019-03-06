package cloudformation.resource.database

import annotation.annotations.database.DatabaseLanguage
import annotation.annotations.database.DatabaseSize
import annotation.annotations.database.RelationalDatabase

data class DatabaseConfiguration(
        val name: String,
        val username: String,
        val password: String,
        val databaseLanguage: DatabaseLanguage,
        val databaseSize: DatabaseSize,
        val size: Int
) {

    companion object {

        @JvmStatic
        fun fromRelationDatabase(relationalDatabase: RelationalDatabase) : DatabaseConfiguration {
            val username = getValue(relationalDatabase.username)
            val password = getValue(relationalDatabase.password)

            return DatabaseConfiguration(
                    relationalDatabase.name,
                    username,
                    password,
                    relationalDatabase.databaseLanguage,
                    relationalDatabase.databaseSize,
                    relationalDatabase.allocatedSizeGB
            )

        }

        private fun getValue(value: String): String {
            return if (value.startsWith("\${") && value.endsWith("}")) {
                val envKey = value.substring(2, value.length - 1)
                println(envKey)
                return System.getenv(envKey)
            } else {
                value
            }
        }
    }



}