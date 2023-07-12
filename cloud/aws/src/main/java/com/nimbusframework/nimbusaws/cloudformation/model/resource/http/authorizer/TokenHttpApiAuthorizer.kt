package com.nimbusframework.nimbusaws.cloudformation.model.resource.http.authorizer

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.nimbusframework.nimbusaws.cloudformation.model.resource.function.FunctionResource
import com.nimbusframework.nimbusaws.cloudformation.model.resource.function.FunctionTrigger
import com.nimbusframework.nimbusaws.cloudformation.model.resource.http.HttpApi
import com.nimbusframework.nimbuscore.persisted.NimbusState

class TokenHttpApiAuthorizer(
    val function: FunctionResource,
    val identityHeader: String,
    restApi: HttpApi,
    ttlSeconds: Int = 300,
    nimbusState: NimbusState,
    stage: String
) : HttpApiAuthorizer(restApi, ttlSeconds, nimbusState, stage), FunctionTrigger {

    override fun addSpecificProperties(properties: JsonObject) {
        properties.addProperty("AuthorizerType", "REQUEST")
        properties.addProperty("AuthorizerPayloadFormatVersion", "1.0")
        properties.add("AuthorizerUri", function.getUri())
        val identitySource = JsonArray()
        identitySource.add("\$request.header.$identityHeader")
        properties.add("IdentitySource", identitySource)
    }

    override fun applyToRestMethodProperties(restMethodProperties: JsonObject) {
        restMethodProperties.addProperty("AuthorizationType", "CUSTOM")
        restMethodProperties.add("AuthorizerId", getId())
    }

    override fun getTriggerType(): String {
        return "apigateway."
    }

    override fun getTriggerName(): String {
        return "HttpAuth"
    }

    override fun getTriggerArn(): JsonObject {
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

        join.add(httpApi.getRef())

        join.add("/authorizers/")

        join.add(getRef())

        joinFunc.add(join)

        arn.add("Fn::Join", joinFunc)

        return arn
    }

}
