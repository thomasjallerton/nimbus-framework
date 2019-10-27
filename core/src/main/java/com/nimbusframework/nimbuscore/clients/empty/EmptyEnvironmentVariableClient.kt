package com.nimbusframework.nimbuscore.clients.empty

import com.nimbusframework.nimbuscore.clients.function.EnvironmentVariableClient
import com.nimbusframework.nimbuscore.exceptions.PermissionException

class EmptyEnvironmentVariableClient: EnvironmentVariableClient {
    private val clientName = "EnvironmentVariableClient"
    override fun containsKey(key: String): Boolean {
        throw PermissionException(clientName)
    }

    override fun get(key: String): String? {
        throw PermissionException(clientName)
    }
}