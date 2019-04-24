package com.nimbusframework.nimbuscore.clients.rdbms

import com.nimbusframework.nimbuscore.clients.PermissionException
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