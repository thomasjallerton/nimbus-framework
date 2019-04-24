package com.nimbusframework.nimbuscore.clients.function

import com.nimbusframework.nimbuscore.clients.PermissionException

class EmptyEnvironmentVariableClient: EnvironmentVariableClient {
    private val clientName = "EnvironmentVariableClient"
    override fun containsKey(key: String): Boolean {
        throw PermissionException(clientName)
    }

    override fun get(key: String): String? {
        throw PermissionException(clientName)
    }
}