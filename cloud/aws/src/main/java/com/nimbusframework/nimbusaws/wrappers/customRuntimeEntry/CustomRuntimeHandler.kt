package com.nimbusframework.nimbusaws.wrappers.customRuntimeEntry

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class CustomRuntimeHandler(
    private val objectMapper: ObjectMapper
) {

    private val REQUEST_ID_HEADER = "lambda-runtime-aws-request-id"
    private val httpClient = HttpClient.newHttpClient()

    @Throws(IOException::class, InterruptedException::class)
    fun getInvocation(endpoint: String): InvocationResponse {
        val request = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(String.format("http://%s/2018-06-01/runtime/invocation/next", endpoint)))
            .build()
        val response: HttpResponse<String> =
            httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
            )
        val requestId = response.headers()
            .firstValue(REQUEST_ID_HEADER).orElseThrow()
        return InvocationResponse(requestId, response.body(), endpoint)
    }

    fun sendResponse(response: Any?, invocation: InvocationResponse) {
        // Post to Lambda success endpoint
        val responseString = if (response == null) {
            ""
        } else {
            objectMapper.writeValueAsString(response)
        }
        val request = HttpRequest.newBuilder()
            .POST(
                HttpRequest.BodyPublishers.ofString(responseString)
            )
            .uri(
                URI.create(String.format("http://%s/2018-06-01/runtime/invocation/%s/response", invocation.endpoint, invocation.requestId))
            )
            .build()

        httpClient.send(
            request,
            HttpResponse.BodyHandlers.ofString()
        )
    }

    @Throws(IOException::class, InterruptedException::class)
    fun handleException(invocation: InvocationResponse, exception: Exception) {
        val errorBody: String =
            objectMapper.writeValueAsString(
                mapOf(Pair("error", exception.message))
            )
        val errorResponse = APIGatewayProxyResponseEvent()
        errorResponse.statusCode = 500
        errorResponse.body = errorBody

        // Post to Lambda error endpoint
        val request = HttpRequest.newBuilder()
            .POST(
                HttpRequest.BodyPublishers.ofString(
                    objectMapper.writeValueAsString(
                        errorResponse
                    )
                )
            )
            .uri(
                URI.create(
                    java.lang.String.format(
                        "http://%s/2018-06-01/runtime/invocation/%s/error",
                        invocation.endpoint,
                        invocation.requestId
                    )
                )
            )
            .build()
        httpClient.send(
            request,
            HttpResponse.BodyHandlers.ofString()
        )
    }

}
