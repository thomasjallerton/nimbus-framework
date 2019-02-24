package clients.rdbms

import annotation.annotations.database.RelationalDatabase
import java.sql.Connection
import java.sql.DriverManager

class DatabaseClientRds<T>(private val databaseObject: Class<T>) {

    fun getConnection(): Connection {
        val relationalDatabase = databaseObject.getDeclaredAnnotation(RelationalDatabase::class.java)
        println("KEY: ${relationalDatabase.name}RdsInstance_CONNECTION_URL")
        val url = System.getenv("${relationalDatabase.name}RdsInstance_CONNECTION_URL")
        println("Url: $url")
        println("Username: ${relationalDatabase.username}")
        println("Password: ${relationalDatabase.password}")
        return DriverManager.getConnection("jdbc:mysql://$url", relationalDatabase.username, relationalDatabase.password)
    }
}
