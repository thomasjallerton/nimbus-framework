package com.nimbusframework.nimbusaws.cloudformation.generation.resources.environment

import com.nimbusframework.nimbusaws.annotation.annotations.parsed.ParsedEnvironmentVariable

class CustomEnvironmentVariable(
    customEnvironmentVariable: ParsedEnvironmentVariable
): NimbusEnvironmentVariable<ParsedEnvironmentVariable>(customEnvironmentVariable) {

    override fun getKey(): String {
        return annotation.key
    }

}
