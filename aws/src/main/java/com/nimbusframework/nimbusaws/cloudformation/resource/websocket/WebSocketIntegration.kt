package com.nimbusframework.nimbusaws.cloudformation.resource.websocket

import com.nimbusframework.nimbusaws.cloudformation.resource.Resource
import com.nimbusframework.nimbusaws.cloudformation.resource.function.FunctionResource
import com.google.gson.JsonObject
import com.nimbusframework.nimbuscore.persisted.NimbusState

class WebSocketIntegration (
        private val webSocketApi: WebSocketApi,
        private val function: FunctionResource,
        private val routeKey: String,
        nimbusState: NimbusState
): Resource(nimbusState, webSocketApi.stage) {

    override fun getName(): String {
        return "${function.getShortName()}WebSocketIntegration${routeKey.replace("$", "")}"
    }

    override fun toCloudFormation(): JsonObject {
        val functionIntegration = JsonObject()
        functionIntegration.addProperty("Type", "AWS::ApiGatewayV2::Integration")

        val properties = getProperties()
        properties.add("ApiId", webSocketApi.getRef())
        properties.addProperty("IntegrationType", "AWS_PROXY")
        properties.add("IntegrationUri", function.getUri())

        functionIntegration.add("Properties", properties)

        return functionIntegration
    }

}