package com.nimbusframework.nimbuslocal.deployment.http

import com.nimbusframework.nimbuscore.annotations.function.HttpMethod
import com.nimbusframework.nimbuscore.clients.JacksonClient

data class HttpRequest(
        var path: String,
        var method: HttpMethod,
        var body: String = "null",
        var headers: Map<String, List<String>> = mapOf()
) {
    constructor(path: String, method: HttpMethod): this(path, method, "null")
    constructor(path: String, method: HttpMethod, body: String): this(path, method, body, mapOf())

    fun setBodyFromObject(body: Any) {
        this.body = JacksonClient.writeValueAsString(body)
    }

    fun withBodyFromObject(body: Any): HttpRequest {
        this.body = JacksonClient.writeValueAsString(body)
        return this
    }
}
