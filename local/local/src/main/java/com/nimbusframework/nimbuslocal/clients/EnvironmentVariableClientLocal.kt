package com.nimbusframework.nimbuslocal.clients

import com.nimbusframework.nimbuscore.clients.function.EnvironmentVariableClient

class EnvironmentVariableClientLocal: EnvironmentVariableClient, LocalClient() {

    override fun canUse(): Boolean {
        functionEnvironment = getCallingServerlessMethod()
        return true
    }

    override val clientName: String = EnvironmentVariableClient::class.java.simpleName

    override fun get(key: String): String? {
        checkClientUse()
        if (functionEnvironment != null) {
            return functionEnvironment!!.getEnvironmentVariables()[key]
        }
        return null
    }

    override fun containsKey(key: String): Boolean {
        checkClientUse()
        if (functionEnvironment != null) {
            return functionEnvironment!!.getEnvironmentVariables().containsKey(key)
        }
        return false
    }
}