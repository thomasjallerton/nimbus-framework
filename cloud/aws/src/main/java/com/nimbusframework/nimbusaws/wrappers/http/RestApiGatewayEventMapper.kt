package com.nimbusframework.nimbusaws.wrappers.http

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent
import com.nimbusframework.nimbuscore.eventabstractions.HttpEvent

object RestApiGatewayEventMapper {

    @JvmStatic
    fun getHttpEvent(request: APIGatewayV2HTTPEvent, requestId: String): HttpEvent {
        return HttpEvent(
            request.headers,
            request.queryStringParameters,
            request.pathParameters,
            request.stageVariables,
            request.requestContext?.authorizer?.lambda,
            request.body,
            request.isBase64Encoded,
            requestId
        )
    }

    @JvmStatic
    fun logSource(event: HttpEvent) {
        val origin = event.headers?.get("Origin") ?: event.headers?.get("origin")
        val userAgent = event.headers?.get("User-Agent") ?: event.headers?.get("user-agent")
        println("Origin: $origin")
        println("User-Agent: $userAgent")
    }

    @JvmStatic
    fun logPathParameters(event: HttpEvent) {
        println("Path Parameters: ${event.pathParameters}")
    }

    @JvmStatic
    fun logQueryStringParameters(event: HttpEvent) {
        println("Query String Parameters: ${event.queryStringParameters}")
    }
}
