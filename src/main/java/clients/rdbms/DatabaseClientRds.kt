package clients.rdbms

import annotation.annotations.database.RelationalDatabase
import java.sql.Connection
import java.sql.DriverManager

class DatabaseClientRds<T>(private val databaseObject: Class<T>) {

    fun getConnection(): Connection {
        val relationalDatabase = databaseObject.getDeclaredAnnotation(RelationalDatabase::class.java)
        println("KEY: ${relationalDatabase.name}RdsInstance_CONNECTION_URL")
        val url = System.getenv("${relationalDatabase.name}RdsInstance_CONNECTION_URL")
        val username = System.getenv("${relationalDatabase.name}RdsInstance_USERNAME")
        val password = System.getenv("${relationalDatabase.name}RdsInstance_PASSWORD")
        println("Url: $url")
        println("Username: $username")
        println("Password: $password")
        return DriverManager.getConnection("jdbc:mysql://$url", username, password)
    }

    fun getConnection(databaseName: String, createIfNotExist: Boolean): Connection {
        val relationalDatabase = databaseObject.getDeclaredAnnotation(RelationalDatabase::class.java)
        println("KEY: ${relationalDatabase.name}RdsInstance_CONNECTION_URL")
        val url = System.getenv("${relationalDatabase.name}RdsInstance_CONNECTION_URL")
        val username = System.getenv("${relationalDatabase.name}RdsInstance_USERNAME")
        val password = System.getenv("${relationalDatabase.name}RdsInstance_PASSWORD")
        println("Url: $url")
        println("Username: $username")
        println("Password: $password")
        return DriverManager.getConnection("jdbc:mysql://$url/$databaseName?createDatabaseIfNotExist=$createIfNotExist", username, password)
    }
}
