package com.nimbusframework.nimbusaws.cloudformation.generation.resources.environment

import com.nimbusframework.nimbusaws.annotation.annotations.parsed.ParsedAnnotation

abstract class NimbusEnvironmentVariable<A : ParsedAnnotation> protected constructor(protected val annotation: A) {

    abstract fun getKey(): String

    fun toValidEnvironmentVariableKey(str: String): String {
        return str
            .replace("-", "_")
            .replace(Regex("[^a-zA-Z0-9_]"), "")
            .uppercase()
    }

}
