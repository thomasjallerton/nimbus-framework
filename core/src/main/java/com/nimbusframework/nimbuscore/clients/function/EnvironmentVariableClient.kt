package com.nimbusframework.nimbuscore.clients.function

interface EnvironmentVariableClient {

    fun containsKey(key: String): Boolean

    fun get(key: String): String?

}
