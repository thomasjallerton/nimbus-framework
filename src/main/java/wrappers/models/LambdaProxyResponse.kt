package wrappers.models

data class LambdaProxyResponse(
        var statusCode: Int = 200,
        var headers: Map<String, String> = mutableMapOf(),
        var body: String = "{}",
        var isBase64Encoded: Boolean = false) {

    companion object {
        fun serverErrorResponse(): LambdaProxyResponse {
            return LambdaProxyResponse(502, mutableMapOf(), "{\"message\":\"server error\"}", false)
        }
    }
}


