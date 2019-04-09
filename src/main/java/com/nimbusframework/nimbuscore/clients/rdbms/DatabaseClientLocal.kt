package com.nimbusframework.nimbuscore.clients.rdbms

import com.nimbusframework.nimbuscore.annotation.annotations.database.RelationalDatabase
import com.nimbusframework.nimbuscore.clients.LocalClient
import com.nimbusframework.nimbuscore.testing.function.PermissionType
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
        val relationalDatabase = databaseObject.getDeclaredAnnotation(RelationalDatabase::class.java)
        val username = "root"
        val password = ""
        return DriverManager.getConnection("jdbc:h2:mem:${relationalDatabase.name};DB_CLOSE_DELAY=-1", username, password)
    }

    override fun getConnection(databaseName: String, createIfNotExist: Boolean): Connection {
        checkClientUse()
        Class.forName("org.h2.Driver").newInstance()
        val relationalDatabase = databaseObject.getDeclaredAnnotation(RelationalDatabase::class.java)
        val username = "root"
        val password = ""
        return DriverManager.getConnection("jdbc:h2:mem:${relationalDatabase.name}$databaseName;DB_CLOSE_DELAY=-1",
                username,
                password)
    }
}