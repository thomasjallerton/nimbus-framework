package com.nimbusframework.nimbusaws.cloudformation.model.resource.http

import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbusaws.cloudformation.model.resource.function.FunctionTrigger
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.nimbusframework.nimbusaws.annotation.processor.ProcessingData
import com.nimbusframework.nimbusaws.cloudformation.model.resource.Resource
import com.nimbusframework.nimbuscore.annotations.http.CorsInformation
import com.nimbusframework.nimbuscore.annotations.http.HttpMethod

/**
 * https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-resource-apigatewayv2-api.html
 */
class HttpApi(
        nimbusState: NimbusState,
        stage: String,
        private val processingData: ProcessingData
): Resource(nimbusState, stage) {

    // https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-properties-apigatewayv2-api-cors.html
    fun createCorsConfiguration(): JsonObject {
        val cors = JsonObject()
        val allowHeaders = JsonArray()
        for (header in CorsInformation.allowedHeaders) {
            allowHeaders.add(header)
        }
        for (header in processingData.defaultRequestHeaders[stage] ?: listOf()) {
            allowHeaders.add(header)
        }

        val allowOrigins = JsonArray()
        if (processingData.defaultAllowedOrigin[stage]?.isNotEmpty() == true) {
            allowOrigins.add(processingData.defaultAllowedOrigin[stage]!!)
        } else {
            allowOrigins.add("*")
        }

        val allowMethods = JsonArray()
        allowMethods.add("*")

        cors.add("AllowHeaders", allowHeaders)
        cors.add("AllowOrigins", allowOrigins)
        cors.add("AllowMethods", allowMethods)
        cors.addProperty("MaxAge", 500)

        return cors
    }

    override fun toCloudFormation(): JsonObject {
        val restApi = JsonObject()
        restApi.addProperty("Type", "AWS::ApiGatewayV2::Api")

        val properties = getProperties()
        properties.addProperty("Name", nimbusState.projectName + "-" + stage + "-" + "HTTP")
        properties.addProperty("ProtocolType", "HTTP");
        properties.add("CorsConfiguration", createCorsConfiguration())

        restApi.add("Properties", properties)

        return restApi
    }

    override fun getArn(suffix: String): JsonObject {
        val arn = JsonObject()
        val joinFunc = JsonArray()
        joinFunc.add("")

        val join = JsonArray()
        join.add("arn:")

        join.add(getRefProperty("AWS::Partition"))

        join.add(":execute-api:")

        join.add(getRefProperty("AWS::Region"))


        join.add(":")

        join.add(getRefProperty("AWS::AccountId"))

        join.add(":")

        join.add(getRef())

        join.add(suffix)

        joinFunc.add(join)

        arn.add("Fn::Join", joinFunc)

        return arn
    }

    override fun getName(): String {
        return "ApiGatewayHttpApi"
    }

}
