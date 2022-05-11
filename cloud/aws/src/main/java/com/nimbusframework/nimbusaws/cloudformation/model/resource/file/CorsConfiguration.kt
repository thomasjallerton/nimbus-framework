package com.nimbusframework.nimbusaws.cloudformation.model.resource.file

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.nimbusframework.nimbuscore.annotations.function.HttpMethod

class CorsConfiguration(
        private val allowedCorsOrigins: Array<String>
) {

    fun toJson(): JsonObject {
        val corsConfiguration = JsonObject()

        val corsRules = JsonArray()

        val corsRule = JsonObject()

        val allowedOrigins = JsonArray()
        allowedCorsOrigins.forEach {
            allowedOrigins.add(it)
        }
        corsRule.add("AllowedOrigins", allowedOrigins)

        val allowedMethods = JsonArray()
        HttpMethod.values().forEach { allowedMethods.add(it.name) }
        corsRule.add("AllowedMethods", allowedMethods)

        corsRules.add(corsRule)

        corsConfiguration.add("CorsRules", corsRules)

        return corsConfiguration
    }


}
