package com.nimbusframework.nimbuslocal.clients

import com.nimbusframework.nimbuscore.annotations.database.RelationalDatabaseDefinition
import com.nimbusframework.nimbuscore.clients.database.DatabaseClient
import com.nimbusframework.nimbuscore.permissions.PermissionType
import java.sql.Connection
import java.sql.DriverManager

internal class DatabaseClientLocal<T>(private val databaseObject: Class<T>): DatabaseClient, LocalClient() {

    override fun canUse(): Boolean {
        return checkPermissions(PermissionType.RELATIONAL_DATABASE, databaseObject.canonicalName)
    }

    override val clientName: String = DatabaseClient::class.java.simpleName

    override fun getConnection(): Connection {
        checkClientUse()
        Class.forName("org.h2.Driver").newInstance()
        val relationalDatabase = databaseObject.getDeclaredAnnotation(RelationalDatabaseDefinition::class.java)
        val username = "root"
        val password = ""
        return DriverManager.getConnection("jdbc:h2:mem:${relationalDatabase.name};DB_CLOSE_DELAY=-1", username, password)
    }

    override fun getConnection(databaseName: String, createIfNotExist: Boolean): Connection {
        checkClientUse()
        Class.forName("org.h2.Driver").newInstance()
        val relationalDatabase = databaseObject.getDeclaredAnnotation(RelationalDatabaseDefinition::class.java)
        val username = "root"
        val password = ""
        return DriverManager.getConnection("jdbc:h2:mem:${relationalDatabase.name}$databaseName;DB_CLOSE_DELAY=-1",
                username,
                password)
    }
}