package wrappers.models

data class LambdaProxyResponse(
        var statusCode: Int = 200,
        var headers: MutableMap<String, String> = mutableMapOf(),
        var body: String = "",
        var isBase64Encoded: Boolean = false)
