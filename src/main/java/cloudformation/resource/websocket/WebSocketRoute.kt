package cloudformation.resource.websocket

import cloudformation.resource.Resource
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import persisted.NimbusState

class WebSocketRoute (
        private val webSocketApi: WebSocketApi,
        private val integration: WebSocketIntegration,
        private val routeKey: String,
        nimbusState: NimbusState
): Resource(nimbusState, webSocketApi.stage) {

    override fun getName(): String {
        return "${routeKey.replace("\$", "")}Route"
    }

    override fun toCloudFormation(): JsonObject {
        val functionIntegration = JsonObject()
        functionIntegration.addProperty("Type", "AWS::ApiGatewayV2::Route")

        val properties = getProperties()
        properties.add("ApiId", webSocketApi.getRef())
        properties.addProperty("RouteKey", routeKey)
        properties.addProperty("AuthorizationType", "NONE")

        val joinValues = JsonArray()
        joinValues.add("integrations")
        joinValues.add(integration.getRef())

        properties.add("Target", joinJson("/", joinValues))

        functionIntegration.add("Properties", properties)

        return functionIntegration
    }

}