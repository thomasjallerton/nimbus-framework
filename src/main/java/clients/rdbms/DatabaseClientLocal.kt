package clients.rdbms

import annotation.annotations.database.RelationalDatabase
import java.sql.Connection
import java.sql.DriverManager

internal class DatabaseClientLocal<T>(private val databaseObject: Class<T>): DatabaseClient {

    override fun getConnection(): Connection {
        Class.forName("org.h2.Driver").newInstance()
        val relationalDatabase = databaseObject.getDeclaredAnnotation(RelationalDatabase::class.java)
        val username = "root"
        val password = ""
        return DriverManager.getConnection("jdbc:h2:mem:${relationalDatabase.name};DB_CLOSE_DELAY=-1", username, password)
    }

    override fun getConnection(databaseName: String, createIfNotExist: Boolean): Connection {
        Class.forName("org.h2.Driver").newInstance()
        val relationalDatabase = databaseObject.getDeclaredAnnotation(RelationalDatabase::class.java)
        val username = "root"
        val password = ""
        return DriverManager.getConnection("jdbc:h2:mem:${relationalDatabase.name}$databaseName;DB_CLOSE_DELAY=-1",
                username,
                password)
    }
}