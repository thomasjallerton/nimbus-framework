package cloudformation.resource.websocket

import cloudformation.resource.Resource
import cloudformation.resource.function.FunctionTrigger
import com.google.gson.JsonObject
import persisted.NimbusState
import java.util.*

class WebSocketApi(
        nimbusState: NimbusState,
        stage: String
): Resource(nimbusState, stage), FunctionTrigger {

    override fun getTriggerType(): String {
        return "apigateway."
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

}