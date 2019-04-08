package com.nimbusframework.nimbuscore.testing.http

import com.nimbusframework.nimbuscore.annotation.annotations.function.HttpMethod
import com.fasterxml.jackson.databind.ObjectMapper

data class HttpRequest(
        var path: String,
        var method: HttpMethod,
        var body: String = "null",
        var pathParameters: Map<String, String> = mapOf(),
        var queryStringParams: Map<String, String> = mapOf(),
        var headers: Map<String, String> = mapOf()
) {
    constructor(path: String, method: HttpMethod): this(path, method, "null")
    constructor(path: String, method: HttpMethod, body: String): this(path, method, body, mapOf())

    fun setBodyFromObject(body: Any) {
        this.body = ObjectMapper().writeValueAsString(body)
    }
}