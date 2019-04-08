package com.nimbusframework.nimbuscore.clients.websocket

import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApi
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApiClientBuilder
import com.amazonaws.services.apigatewaymanagementapi.model.PostToConnectionRequest
import com.fasterxml.jackson.databind.ObjectMapper
import java.nio.ByteBuffer

internal class ServerlessFunctionWebSocketClientApiGateway: ServerlessFunctionWebSocketClient {

    private val env = System.getenv()
    private val endpoint = if (env.containsKey("WEBSOCKET_ENDPOINT")) env["WEBSOCKET_ENDPOINT"]!! else ""
    private val region = if (env.containsKey("AWS_DEFAULT_REGION")) env["AWS_DEFAULT_REGION"]!! else ""

    private var apiGatewayManagementApi: AmazonApiGatewayManagementApi? = null

    private val objectMapper = ObjectMapper()

    override fun sendToConnection(connectionId: String, data: ByteBuffer) {
        initialiseApiGatewayManagementClient()
        val postToConnectionRequest = PostToConnectionRequest()
                .withConnectionId(connectionId)
                .withData(data)

        apiGatewayManagementApi!!.postToConnection(postToConnectionRequest)
    }

    override fun sendToConnectionConvertToJson(connectionId: String, data: Any) {
        initialiseApiGatewayManagementClient()
        val json = objectMapper.writeValueAsBytes(data)

        val buffer = ByteBuffer.wrap(json)

        val postToConnectionRequest = PostToConnectionRequest()
                .withConnectionId(connectionId)
                .withData(buffer)
        apiGatewayManagementApi!!.postToConnection(postToConnectionRequest)
    }

    private fun initialiseApiGatewayManagementClient() {
        if (apiGatewayManagementApi == null) {
            apiGatewayManagementApi = AmazonApiGatewayManagementApiClientBuilder
                    .standard()
                    .withEndpointConfiguration(
                            AwsClientBuilder.EndpointConfiguration(
                                    endpoint,
                                    region
                            )
                    ).build()
        }
    }
}