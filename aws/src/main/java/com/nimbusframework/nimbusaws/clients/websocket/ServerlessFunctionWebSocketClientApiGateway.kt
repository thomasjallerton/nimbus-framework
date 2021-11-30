package com.nimbusframework.nimbusaws.clients.websocket

import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusframework.nimbuscore.clients.websocket.ServerlessFunctionWebSocketClient
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.services.apigatewaymanagementapi.ApiGatewayManagementApiClientBuilder
import software.amazon.awssdk.services.apigatewaymanagementapi.model.PostToConnectionRequest
import java.net.URI
import java.nio.ByteBuffer

internal class ServerlessFunctionWebSocketClientApiGateway(
    private val apiGatewayManagementApiBuilder: ApiGatewayManagementApiClientBuilder
): ServerlessFunctionWebSocketClient {

    private val env = System.getenv()
    private val endpoint = if (env.containsKey("WEBSOCKET_ENDPOINT")) env["WEBSOCKET_ENDPOINT"]!! else ""

    private val objectMapper = ObjectMapper()

    private val apiGatewayManagementApi by lazy {
        apiGatewayManagementApiBuilder.endpointOverride(URI.create(endpoint)).build()
    }

    override fun sendToConnection(connectionId: String, data: ByteBuffer) {
        val postToConnectionRequest = PostToConnectionRequest.builder()
            .connectionId(connectionId)
            .data(SdkBytes.fromByteBuffer(data))
            .build()

        apiGatewayManagementApi.postToConnection(postToConnectionRequest)
    }

    override fun sendToConnectionConvertToJson(connectionId: String, data: Any) {
        val json = objectMapper.writeValueAsBytes(data)

        val postToConnectionRequest = PostToConnectionRequest.builder()
            .connectionId(connectionId)
            .data(SdkBytes.fromByteArray(json))
            .build()
        apiGatewayManagementApi.postToConnection(postToConnectionRequest)
    }
}
