package cloudformation.outputs

import cloudformation.resource.websocket.WebSocketApi

import configuration.WEBSOCKET_API_URL_OUTPUT
import persisted.NimbusState

class WebSocketApiOutput(
        webSocketApi: WebSocketApi,
        nimbusState: NimbusState
): ApiGatewayOutput(
        webSocketApi,
        nimbusState,
        "wss://"
) {
    override fun getExportName(): String {
        return "${nimbusState.projectName}-$stage-$WEBSOCKET_API_URL_OUTPUT"
    }
}