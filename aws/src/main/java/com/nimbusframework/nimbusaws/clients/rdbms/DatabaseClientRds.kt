package com.nimbusframework.nimbusaws.clients.rdbms

import com.google.inject.Inject
import com.nimbusframework.nimbuscore.annotations.database.DatabaseLanguage
import com.nimbusframework.nimbuscore.annotations.database.RelationalDatabaseDefinition
import com.nimbusframework.nimbuscore.clients.database.DatabaseClient
import com.nimbusframework.nimbuscore.clients.function.EnvironmentVariableClient
import java.sql.Connection
import java.sql.DriverManager

internal class DatabaseClientRds<T>(private val databaseObject: Class<T>): DatabaseClient {

    @Inject
    private lateinit var environmentVariableClient: EnvironmentVariableClient

    override fun getConnection(): Connection {
        val relationalDatabase = databaseObject.getDeclaredAnnotation(RelationalDatabaseDefinition::class.java)
        val url = environmentVariableClient.get("${relationalDatabase.name}RdsInstance_CONNECTION_URL")
        val username = environmentVariableClient.get("${relationalDatabase.name}RdsInstance_USERNAME")
        val password = environmentVariableClient.get("${relationalDatabase.name}RdsInstance_PASSWORD")
        val languageParam = languageToParameter(relationalDatabase.databaseLanguage)
        return DriverManager.getConnection("jdbc:$languageParam://${appendSlash(url!!)}", username, password)
    }

    override fun getConnection(databaseName: String, createIfNotExist: Boolean): Connection {
        val relationalDatabase = databaseObject.getDeclaredAnnotation(RelationalDatabaseDefinition::class.java)
        val url = environmentVariableClient.get("${relationalDatabase.name}RdsInstance_CONNECTION_URL")
        val username = environmentVariableClient.get("${relationalDatabase.name}RdsInstance_USERNAME")
        val password = environmentVariableClient.get("${relationalDatabase.name}RdsInstance_PASSWORD")
        val languageParam = languageToParameter(relationalDatabase.databaseLanguage)
        return DriverManager.getConnection("jdbc:$languageParam://${appendSlash(url!!)}$databaseName?createDatabaseIfNotExist=$createIfNotExist", username, password)
    }

    private fun languageToParameter(language: DatabaseLanguage): String {
        return when(language) {
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
