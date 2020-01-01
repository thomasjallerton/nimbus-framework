package com.nimbusframework.nimbusaws.cloudformation.resource.websocket

import com.nimbusframework.nimbusaws.cloudformation.resource.Resource
import com.nimbusframework.nimbusaws.cloudformation.resource.function.FunctionTrigger
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.nimbusframework.nimbuscore.persisted.NimbusState

class WebSocketApi(
        nimbusState: NimbusState,
        stage: String
): Resource(nimbusState, stage), FunctionTrigger {

    override fun getTriggerType(): String {
        return "apigateway."
    }

    override fun getTriggerName(): String {
        return "WebSocket"
    }

    override fun getTriggerArn(): JsonObject {
        return JsonObject()
    }

    override fun getName(): String {
        return "WebsocketApi"
    }

    override fun toCloudFormation(): JsonObject {
        val webSocketApi = JsonObject()
        webSocketApi.addProperty("Type", "AWS::ApiGatewayV2::Api")

        val properties = getProperties()
        properties.addProperty("Name", nimbusState.projectName + "-" + stage + "-" + "WebSocket")
        properties.addProperty("ProtocolType", "WEBSOCKET")
        properties.addProperty("RouteSelectionExpression", "\$request.body.topic")

        webSocketApi.add("Properties", properties)

        return webSocketApi
    }

    fun getEndpoint(): JsonObject {

        val joinValues = JsonArray()
        joinValues.add("https://")
        joinValues.add(getRef())
        joinValues.add(".execute-api.")
        joinValues.add(getRegion())
        joinValues.add(".amazonaws.com/$stage")

        return joinJson("", joinValues)
    }

    override fun getArn(suffix: String): JsonObject {
        val strArn = "arn:aws:execute-api:\${AWS::Region}:\${AWS::AccountId}:\${${getName()}}$suffix"
        return subFunc(strArn)
    }
}