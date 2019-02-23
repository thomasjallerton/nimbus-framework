package testing.http

import annotation.annotations.function.HttpMethod

data class HttpRequest(
        var path: String,
        var method: HttpMethod,
        var body: Any? = null,
        var pathParameters: Map<String, String> = mapOf(),
        var queryStringParams: Map<String, String> = mapOf(),
        var headers: Map<String, String> = mapOf()
) {
    constructor(path: String, method: HttpMethod): this(path, method, null)
    constructor(path: String, method: HttpMethod, body: Any): this(path, method, body, mapOf())
}