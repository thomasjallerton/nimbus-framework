package wrappers.http.models

import com.fasterxml.jackson.databind.ObjectMapper

class LambdaProxyResponseBuilder {
    private val lambdaProxyResponse = LambdaProxyResponse()

    fun withStatusCode(statusCode: Int): LambdaProxyResponseBuilder {
        lambdaProxyResponse.statusCode = statusCode
        return this
    }

    fun withBody(body: String): LambdaProxyResponseBuilder {
        lambdaProxyResponse.body = body
        return this
    }

    fun withBody(obj: Any): LambdaProxyResponseBuilder {
        val mapper = ObjectMapper()
        lambdaProxyResponse.body =  mapper.writeValueAsString(obj)
        return this
    }

    fun isBase64Encoded(): LambdaProxyResponseBuilder {
        lambdaProxyResponse.isBase64Encoded = true
        return this
    }

    fun isNotBase64Encoded(): LambdaProxyResponseBuilder {
        lambdaProxyResponse.isBase64Encoded = false
        return this
    }

    fun withHeaders(headers: Map<String, String>): LambdaProxyResponseBuilder {
        lambdaProxyResponse.headers = headers
        return this
    }

    fun build(): LambdaProxyResponse {
        return lambdaProxyResponse
    }

}