package com.nimbusframework.nimbuscore.testing.http

import com.nimbusframework.nimbuscore.annotation.annotations.function.HttpMethod

data class HttpMethodIdentifier(
        private var path: String,
        private val method: HttpMethod) {

    private val pathParameterNames: MutableList<String> = mutableListOf()

    init {
        val pattern = Regex("\\{([^}]+)}")
        val parameterNames = pattern.findAll(path)

        if (!parameterNames.none()) {
            for (name in parameterNames) {
                pathParameterNames.add(name.groupValues[1])
                path = path.replace(name.value, "([^\\/]+)")
            }
            path = path.replace("/", "\\/")
        }

    }

    fun matches(givenPath: String, method: HttpMethod): Boolean {
        if (method != this.method) return false
        return if (pathParameterNames.isNotEmpty()) {
            path.toRegex().matches(givenPath)
        } else {
            givenPath == path
        }
    }

    fun extractPathParameters(givenPath: String): Map<String, String> {
        return if (pathParameterNames.isNotEmpty()) {
            val matches = path.toRegex().find(givenPath)
            if (matches != null) {
                val pathParameters: MutableMap<String, String> = mutableMapOf()
                for ((index, pathParameterName) in pathParameterNames.withIndex()) {
                    pathParameters[pathParameterName] = matches.groupValues[index + 1]
                }
                pathParameters
            } else {
                mapOf()
            }
        } else {
            mapOf()
        }
    }
}
