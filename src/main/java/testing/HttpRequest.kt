package testing

data class HttpRequest(
        var path: String,
        var method: String,
        var body: Any? = null,
        var pathParameters: Map<String, String> = mapOf(),
        var queryStringParams: Map<String, String> = mapOf(),
        var headers: Map<String, String> = mapOf()
) {
    constructor(path: String, method: String): this(path, method, null)
    constructor(path: String, method: String, body: Any): this(path, method, body, mapOf())
}