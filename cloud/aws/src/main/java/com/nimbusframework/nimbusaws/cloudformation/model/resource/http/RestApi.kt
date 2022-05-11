package com.nimbusframework.nimbusaws.cloudformation.model.resource.http

import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbusaws.cloudformation.model.resource.function.FunctionTrigger
import com.google.gson.JsonArray
import com.google.gson.JsonObject

class RestApi(
        nimbusState: NimbusState,
        stage: String
): AbstractRestResource(nimbusState, stage), FunctionTrigger {

    override fun getTriggerArn(): JsonObject {
        return getArn("/*/*")
    }

    override fun getTriggerType(): String {
        return "apigateway."
    }

    override fun getTriggerName(): String {
        return "RestApi"
    }

    override fun getPath(): String {
        return ""
    }

    override fun getId(): JsonObject {
        val parentId = JsonObject()
        val getAttr = JsonArray()
        getAttr.add("ApiGatewayRestApi")
        getAttr.add("RootResourceId")
        parentId.add("Fn::GetAtt", getAttr)
        return parentId
    }

    override fun getRootId(): JsonObject {
        val rootId = JsonObject()
        rootId.addProperty("Ref", getName())
        return rootId
    }

    override fun toCloudFormation(): JsonObject {
        val restApi = JsonObject()
        restApi.addProperty("Type", "AWS::ApiGateway::RestApi")

        val properties = getProperties()
        properties.addProperty("Name", nimbusState.projectName + "-" + stage + "-" + "HTTP")

        val endpointConfig = JsonObject()
        val types = JsonArray()
        types.add("EDGE")

        endpointConfig.add("Types", types)

        properties.add("EndpointConfiguration", endpointConfig)

        restApi.add("Properties", properties)

        return restApi
    }

    override fun getName(): String {
        return "ApiGatewayRestApi"
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

        join.add(getRootId())

        join.add(suffix)

        joinFunc.add(join)

        arn.add("Fn::Join", joinFunc)

        return arn
    }

}
