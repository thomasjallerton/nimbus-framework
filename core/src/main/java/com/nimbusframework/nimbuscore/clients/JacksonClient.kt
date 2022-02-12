package com.nimbusframework.nimbuscore.clients

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule

object JacksonClient {

    private val json by lazy {
        ObjectMapper().registerModule(JavaTimeModule())
    }

    @JvmStatic
    fun writeValueAsString(any: Any?): String {
        return json.writeValueAsString(any)
    }

    @JvmStatic
    fun writeValueAsBytes(any: Any): ByteArray {
        return json.writeValueAsBytes(any)
    }

    @JvmStatic
    fun <T> readValue(text: String?, expectedType: Class<T>): T {
        return json.readValue(text, expectedType)
    }

    @JvmStatic
    fun <T> convertValue(map: Map<String, Any?>, expectedType: Class<T>): T {
        return json.convertValue(map, expectedType)
    }

    @JvmStatic
    fun readTree(str: String): JsonNode {
        return json.readTree(str)
    }

    @JvmStatic
    fun valueToTree(any: Any): ObjectNode {
        return json.valueToTree(any)
    }

}
