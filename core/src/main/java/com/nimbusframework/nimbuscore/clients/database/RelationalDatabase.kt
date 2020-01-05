package com.nimbusframework.nimbuscore.clients.database

import com.nimbusframework.nimbuscore.clients.ClientBuilder
import java.sql.Connection

open class RelationalDatabase(database: Class<*>): DatabaseClient {

    private val databaseClient = ClientBuilder.getDatabaseClient(database)

    override fun getConnection(): Connection {
        return databaseClient.getConnection()
    }

    override fun getConnection(databaseName: String, createIfNotExist: Boolean): Connection {
        return databaseClient.getConnection(databaseName, createIfNotExist)
    }

}