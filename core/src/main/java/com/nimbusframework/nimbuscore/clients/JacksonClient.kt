package com.nimbusframework.nimbuscore.clients

import com.fasterxml.jackson.jr.ob.JSON

object JacksonClient {

    private val json by lazy {
        JSON.std
    }

    @JvmStatic
    fun writeValueAsString(any: Any?): String {
        return json.asString(any)
    }

    @JvmStatic
    fun writeValueAsBytes(any: Any): ByteArray {
        return json.asBytes(any)
    }

    @JvmStatic
    fun <T> readValue(text: String, expectedType: Class<T>): T {
        return json.beanFrom(expectedType, text)
    }

    @JvmStatic
    fun <T> convertValue(map: Map<String, Any?>, expectedType: Class<T>): T {
        val composer = json.composeString().startObject()
        map.forEach { composer.putObject(it.key, it.value) }

        val jsonStr = composer.end().finish()
        return json.beanFrom(expectedType, jsonStr)
    }

}
