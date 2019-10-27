package com.nimbusframework.nimbusaws.clients.rdbms

import com.nimbusframework.nimbuscore.annotations.database.DatabaseLanguage
import com.nimbusframework.nimbuscore.annotations.database.RelationalDatabase
import com.nimbusframework.nimbuscore.clients.database.DatabaseClient
import java.sql.Connection
import java.sql.DriverManager

internal class DatabaseClientRds<T>(private val databaseObject: Class<T>): DatabaseClient {

    override fun getConnection(): Connection {
        val relationalDatabase = databaseObject.getDeclaredAnnotation(RelationalDatabase::class.java)
        val url = System.getenv("${relationalDatabase.name}RdsInstance_CONNECTION_URL")
        val username = System.getenv("${relationalDatabase.name}RdsInstance_USERNAME")
        val password = System.getenv("${relationalDatabase.name}RdsInstance_PASSWORD")
        val languageParam = languageToParameter(relationalDatabase.databaseLanguage)
        return DriverManager.getConnection("jdbc:$languageParam://$url", username, password)
    }

    override fun getConnection(databaseName: String, createIfNotExist: Boolean): Connection {
        val relationalDatabase = databaseObject.getDeclaredAnnotation(RelationalDatabase::class.java)
        val url = System.getenv("${relationalDatabase.name}RdsInstance_CONNECTION_URL")
        val username = System.getenv("${relationalDatabase.name}RdsInstance_USERNAME")
        val password = System.getenv("${relationalDatabase.name}RdsInstance_PASSWORD")
        val languageParam = languageToParameter(relationalDatabase.databaseLanguage)
        return DriverManager.getConnection("jdbc:$languageParam://$url/$databaseName?createDatabaseIfNotExist=$createIfNotExist", username, password)
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
}
