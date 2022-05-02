package com.nimbusframework.nimbusaws.clients

import com.nimbusframework.nimbusaws.cloudformation.generation.resources.environment.NimbusEnvironmentVariable
import com.nimbusframework.nimbuscore.clients.function.EnvironmentVariableClient

class InternalEnvironmentVariableClient(
    private val environmentVariableClient: EnvironmentVariableClient
) {

    fun get(variable: NimbusEnvironmentVariable<*>): String? {
        return environmentVariableClient.get(variable.getKey())
    }

}
