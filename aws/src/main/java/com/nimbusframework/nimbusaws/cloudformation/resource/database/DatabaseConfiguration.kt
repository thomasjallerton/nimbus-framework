package com.nimbusframework.nimbusaws.cloudformation.resource.database

import com.nimbusframework.nimbuscore.annotations.database.DatabaseLanguage
import com.nimbusframework.nimbuscore.annotations.database.DatabaseSize
import com.nimbusframework.nimbuscore.annotations.database.RelationalDatabase

data class DatabaseConfiguration(
        val name: String,
        val username: String,
        val password: String,
        val databaseLanguage: DatabaseLanguage,
        val databaseClass: DatabaseSize,
        val awsDatabaseClass: String,
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
                    relationalDatabase.databaseClass,
                    relationalDatabase.awsDatabaseInstance,
                    relationalDatabase.allocatedSizeGB
            )

        }

        private fun getValue(value: String): String {
            return if (value.startsWith("\${") && value.endsWith("}")) {
                val envKey = value.substring(2, value.length - 1)
                return System.getenv(envKey) ?: ""
            } else {
                value
            }
        }
    }



}