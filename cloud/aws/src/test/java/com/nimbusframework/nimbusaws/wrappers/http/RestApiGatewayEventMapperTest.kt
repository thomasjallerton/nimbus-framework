package com.nimbusframework.nimbusaws.wrappers.http

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe

class RestApiGatewayEventMapperTest: AnnotationSpec() {

    val underTest = RestApiGatewayEventMapper

    @Test
    fun canParseRestApiGatewayEvent() {
        val headers = mapOf(Pair("headerKey", "headerVal"))
        val queryParams = mapOf(Pair("queryKey", "queryVal"))
        val pathParams = mapOf(Pair("pathKey", "pathVal"))
        val stageVariables = mapOf(Pair("stageKey", "stageVal"))

        val requestEvent = APIGatewayV2HTTPEvent()
        requestEvent.headers = headers
        requestEvent.queryStringParameters = queryParams
        requestEvent.pathParameters = pathParams
        requestEvent.stageVariables = stageVariables
        requestEvent.body = "body"
        requestEvent.isBase64Encoded = true

        val result = underTest.getHttpEvent(requestEvent, "requestId")

        result.headers shouldBe headers
        result.queryStringParameters shouldBe queryParams
        result.pathParameters shouldBe pathParams
        result.stageVariables shouldBe stageVariables
        result.body shouldBe "body"
        result.isBase64Encoded shouldBe true
        result.requestId shouldBe "requestId"
    }
}
