package cloudformation.resource.websocket

import cloudformation.resource.Resource
import com.google.gson.JsonObject
import persisted.NimbusState

class WebSocketStage(
        private val webSocketApi: WebSocketApi,
        private val deployment: WebSocketDeployment,
        nimbusState: NimbusState
): Resource(nimbusState, webSocketApi.stage) {

    override fun toCloudFormation(): JsonObject {
        val stage = JsonObject()
        stage.addProperty("Type", "AWS::ApiGatewayV2::Stage")

        val properties = getProperties()
        properties.add("ApiId", webSocketApi.getRef())
        properties.add("DeploymentId", deployment.getRef())
        properties.addProperty("StageName", webSocketApi.stage)

        stage.add("Properties", properties)
        return stage
    }

    override fun getName(): String {
        return "WebsocketApiStage"
    }
}