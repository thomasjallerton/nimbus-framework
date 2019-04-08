package com.nimbusframework.nimbuscore.cloudformation.outputs

import com.nimbusframework.nimbuscore.cloudformation.resource.websocket.WebSocketApi

import com.nimbusframework.nimbuscore.configuration.WEBSOCKET_API_URL_OUTPUT
import com.nimbusframework.nimbuscore.persisted.NimbusState

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