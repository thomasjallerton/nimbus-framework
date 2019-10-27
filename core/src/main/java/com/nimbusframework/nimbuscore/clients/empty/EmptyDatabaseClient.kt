package com.nimbusframework.nimbuscore.clients.empty

import com.nimbusframework.nimbuscore.clients.database.DatabaseClient
import com.nimbusframework.nimbuscore.exceptions.PermissionException
import java.sql.Connection

class EmptyDatabaseClient: DatabaseClient {
    override fun getConnection(): Connection {
        throw PermissionException(clientName)
    }

    override fun getConnection(databaseName: String, createIfNotExist: Boolean): Connection {
        throw PermissionException(clientName)
    }

    private val clientName = "DatabaseClient"

}