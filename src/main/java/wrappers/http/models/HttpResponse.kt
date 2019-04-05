package wrappers.http.models

import com.fasterxml.jackson.databind.ObjectMapper

data class HttpResponse(
        var statusCode: Int = 200,
        var headers: Map<String, String> = mutableMapOf(),
        var body: String = "{}",
        var isBase64Encoded: Boolean = false) {

    fun withBody(body: String): HttpResponse {
        this.body = body
        return this
    }

    fun withJsonBody(body: Any): HttpResponse {
        this.body = ObjectMapper().writeValueAsString(body)
        return this
    }

    fun withHeaders(headers: Map<String, String>): HttpResponse {
        this.headers = headers
        return this
    }

    fun withStatusCode(code: Int): HttpResponse {
        statusCode = code
        return this
    }

    companion object {
        fun serverErrorResponse(): HttpResponse {
            return HttpResponse(502, mutableMapOf(), "{\"message\":\"server error\"}", false)
        }
    }
}


