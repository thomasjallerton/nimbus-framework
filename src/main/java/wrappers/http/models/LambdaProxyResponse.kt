package wrappers.http.models

data class LambdaProxyResponse(
        var statusCode: Int = 200,
        var headers: Map<String, String> = mutableMapOf(),
        var body: String = "{}",
        var isBase64Encoded: Boolean = false) {

    fun withMessage(msg: String): LambdaProxyResponse {
        body = "{\"message\":\"$msg\"}"
        return this
    }

    fun withStatusCode(code: Int): LambdaProxyResponse {
        statusCode = code
        return this
    }

    companion object {
        fun serverErrorResponse(): LambdaProxyResponse {
            return LambdaProxyResponse(502, mutableMapOf(), "{\"message\":\"server error\"}", false)
        }
    }
}


