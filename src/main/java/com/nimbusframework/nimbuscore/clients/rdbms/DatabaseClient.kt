package com.nimbusframework.nimbuscore.clients.rdbms

import java.sql.Connection

interface DatabaseClient {

    fun getConnection(): Connection
    fun getConnection(databaseName: String, createIfNotExist: Boolean): Connection

}