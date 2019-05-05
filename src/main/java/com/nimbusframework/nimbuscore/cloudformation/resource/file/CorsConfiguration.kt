package com.nimbusframework.nimbuscore.cloudformation.resource.file

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.nimbusframework.nimbuscore.annotation.annotations.function.HttpMethod

class CorsConfiguration(
        private val allowedCorsOrigins: Array<String>,
        private val allowedCorsMethods: Array<HttpMethod>
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
        allowedCorsMethods.forEach { allowedMethods.add(it.toString()) }
        corsRule.add("AllowedMethods", allowedMethods)

        corsRules.add(corsRule)

        corsConfiguration.add("CorsRules", corsRules)

        return corsConfiguration
    }


}