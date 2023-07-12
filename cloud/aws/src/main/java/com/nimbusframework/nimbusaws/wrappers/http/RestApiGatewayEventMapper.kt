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
}
