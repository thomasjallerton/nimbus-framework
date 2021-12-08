package com.nimbusframework.nimbuscore.clients

import com.fasterxml.jackson.databind.ObjectMapper

object JacksonClient {

    private val json by lazy {
        ObjectMapper()
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
    fun <T> readValue(text: String, expectedType: Class<T>): T {
        return json.readValue(text, expectedType)
    }

    @JvmStatic
    fun <T> convertValue(map: Map<String, Any?>, expectedType: Class<T>): T {
        return json.convertValue(map, expectedType)
    }

}
