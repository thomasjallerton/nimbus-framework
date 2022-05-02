package com.nimbusframework.nimbusaws.clients.rdbms

import com.nimbusframework.nimbusaws.annotation.annotations.database.ParsedDatabaseConfig
import com.nimbusframework.nimbusaws.clients.InternalEnvironmentVariableClient
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.database.DatabaseConnectionUrlEnvironmentVariable
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.database.DatabasePasswordEnvironmentVariable
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.database.DatabaseUsernameEnvironmentVariable
import com.nimbusframework.nimbuscore.annotations.database.DatabaseLanguage
import com.nimbusframework.nimbuscore.annotations.database.RelationalDatabaseDefinition
import com.nimbusframework.nimbuscore.clients.database.DatabaseClient
import java.sql.Connection
import java.sql.DriverManager

internal class DatabaseClientRds (
    private val parsedDatabaseConfig: ParsedDatabaseConfig,
    private val internalEnvironmentVariableClient: InternalEnvironmentVariableClient
) : DatabaseClient {


    override fun getConnection(): Connection {
        val url = internalEnvironmentVariableClient.get(DatabaseConnectionUrlEnvironmentVariable(parsedDatabaseConfig))
        val username =internalEnvironmentVariableClient.get(DatabaseUsernameEnvironmentVariable(parsedDatabaseConfig))
        val password = internalEnvironmentVariableClient.get(DatabasePasswordEnvironmentVariable(parsedDatabaseConfig))
        val languageParam = languageToParameter(parsedDatabaseConfig.databaseLanguage)
        return DriverManager.getConnection("jdbc:$languageParam://${appendSlash(url!!)}", username, password)
    }

    override fun getConnection(databaseName: String, createIfNotExist: Boolean): Connection {
        val url = internalEnvironmentVariableClient.get(DatabaseConnectionUrlEnvironmentVariable(parsedDatabaseConfig))
        val username =internalEnvironmentVariableClient.get(DatabaseUsernameEnvironmentVariable(parsedDatabaseConfig))
        val password = internalEnvironmentVariableClient.get(DatabasePasswordEnvironmentVariable(parsedDatabaseConfig))
        val languageParam = languageToParameter(parsedDatabaseConfig.databaseLanguage)
        return DriverManager.getConnection(
            "jdbc:$languageParam://${appendSlash(url!!)}$databaseName?createDatabaseIfNotExist=$createIfNotExist",
            username,
            password
        )
    }

    private fun languageToParameter(language: DatabaseLanguage): String {
        return when (language) {
            DatabaseLanguage.MYSQL -> "mysql"
            DatabaseLanguage.MARIADB -> "mariadb"
            DatabaseLanguage.POSTGRESQL -> "postgresql"
            DatabaseLanguage.ORACLE -> "oracle"
            DatabaseLanguage.SQLSERVER -> "sqlserver"
        }
    }

    private fun appendSlash(str: String): String {
        return if (str.endsWith("/")) {
            str
        } else {
            "$str/"
        }
    }
}
