package com.nimbusframework.nimbusaws.annotation.annotations.database

import com.nimbusframework.nimbusaws.annotation.annotations.parsed.ParsedAnnotation
import com.nimbusframework.nimbuscore.annotations.database.DatabaseLanguage
import com.nimbusframework.nimbuscore.annotations.database.DatabaseSize
import com.nimbusframework.nimbuscore.annotations.database.RelationalDatabaseDefinition

data class ParsedDatabaseConfig(
    val name: String,
    val username: String,
    val password: String,
    val databaseLanguage: DatabaseLanguage,
    val awsDatabaseInstance: String,
    val size: Int
) : ParsedAnnotation {

    companion object {

        @JvmStatic
        fun fromRelationDatabase(relationalDatabase: RelationalDatabaseDefinition): ParsedDatabaseConfig {
            val username = getValue(relationalDatabase.username)
            val password = getValue(relationalDatabase.password)

            return ParsedDatabaseConfig(
                relationalDatabase.name,
                username,
                password,
                relationalDatabase.databaseLanguage,
                toInstanceType(relationalDatabase.databaseClass),
                relationalDatabase.allocatedSizeGB
            )

        }

        @JvmStatic
        fun fromRdsDatabase(rdsDatabase: RdsDatabase): ParsedDatabaseConfig {
            val username = getValue(rdsDatabase.username)
            val password = getValue(rdsDatabase.password)

            return ParsedDatabaseConfig(
                rdsDatabase.name,
                username,
                password,
                rdsDatabase.databaseLanguage,
                rdsDatabase.awsDatabaseInstance,
                rdsDatabase.allocatedSizeGB
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

        fun toInstanceType(databaseSize: DatabaseSize): String {
            return when (databaseSize) {
                DatabaseSize.FREE -> "db.t2.micro"
                DatabaseSize.SMALL -> "db.t2.small"
                DatabaseSize.MEDIUM -> "db.t3.medium"
                DatabaseSize.LARGE -> "db.r4.large"
                DatabaseSize.XLARGE -> "db.r5.2xlarge"
                DatabaseSize.MAXIMUM -> "db.r5.12xlarge"
            }
        }
    }
}
