package com.nimbusframework.nimbusaws.wrappers.http

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe

class RestApiGatewayEventMapperTest: AnnotationSpec() {

    val underTest = RestApiGatewayEventMapper

    @Test
    fun canParseRestApiGatewayEvent() {
        val headers = mapOf(Pair("headerKey", "headerVal"))
        val multiValHeaders = mapOf(Pair("headerKey", listOf("headerVal1", "headerVal2")))
        val queryParams = mapOf(Pair("queryKey", "queryVal"))
        val multiValQuery = mapOf(Pair("queryKey", listOf("queryVal1", "queryVal2")))
        val pathParams = mapOf(Pair("pathKey", "pathVal"))
        val stageVariables = mapOf(Pair("stageKey", "stageVal"))

        val requestEvent = APIGatewayProxyRequestEvent().withResource("resource")
                .withPath("path")
                .withHttpMethod("POST")
                .withHeaders(headers)
                .withMultiValueHeaders(multiValHeaders)
                .withQueryStringParameters(queryParams)
                .withMultiValueQueryStringParameters(multiValQuery)
                .withPathParameters(pathParams)
                .withStageVariables(stageVariables)
                .withBody("body")
                .withIsBase64Encoded(true)

        val result = underTest.getHttpEvent(requestEvent, "requestId")

        result.resource shouldBe "resource"
        result.path shouldBe "path"
        result.httpMethod shouldBe "POST"
        result.headers shouldBe headers
        result.multiValueHeaders shouldBe multiValHeaders
        result.queryStringParameters shouldBe queryParams
        result.multiValueQueryStringParameters shouldBe multiValQuery
        result.pathParameters shouldBe pathParams
        result.stageVariables shouldBe stageVariables
        result.body shouldBe "body"
        result.isBase64Encoded shouldBe true
        result.requestId shouldBe "requestId"
    }
}