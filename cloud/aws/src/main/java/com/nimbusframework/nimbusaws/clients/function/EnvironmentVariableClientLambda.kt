package com.nimbusframework.nimbusaws.clients.function

import com.nimbusframework.nimbuscore.clients.function.EnvironmentVariableClient

class EnvironmentVariableClientLambda: EnvironmentVariableClient {
    override fun containsKey(key: String): Boolean {
        return System.getenv().containsKey(key)
    }

    override fun get(key: String): String? {
        return System.getenv(key)
    }
}