package com.nimbusframework.nimbuscore.wrappers.http.models

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.nimbusframework.nimbuscore.wrappers.ServerlessEvent
import java.util.*

data class HttpEvent(
        val resource: String? = null,
        val path: String? = null,
        val httpMethod: String? = null,
        val headers: Map<String, String>? = mapOf(),
        val multiValueHeaders: Map<String, List<String>>? = mapOf(),
        val queryStringParameters: Map<String, String>? = mapOf(),
        val multiValueQueryStringParameters: Map<String, List<String>>? = mapOf(),
        val pathParameters: Map<String, String>? = mapOf(),
        val stageVariables: Map<String, String>? = mapOf(),
        val body: String? = null,
        val isBase64Encoded: Boolean? = null,
        val requestId: String = UUID.randomUUID().toString()
) : ServerlessEvent {

    constructor(request: APIGatewayProxyRequestEvent, requestId: String) :
            this(
                    request.resource,
                    request.path,
                    request.httpMethod,
                    request.headers,
                    request.multiValueHeaders,
                    request.queryStringParameters,
                    request.multiValueQueryStringParameters,
                    request.pathParameters,
                    request.stageVariables,
                    request.body,
                    request.isBase64Encoded,
                    requestId
            )
}