package com.nimbusframework.nimbusaws.wrappers.http

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.nimbusframework.nimbuscore.eventabstractions.HttpEvent

object RestApiGatewayEventMapper {

    @JvmStatic
    fun getHttpEvent(request: APIGatewayProxyRequestEvent, requestId: String): HttpEvent {
        return HttpEvent(request.resource,
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
}