package com.nimbusframework.nimbusaws.clients.websocket

import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApi
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApiClientBuilder
import com.amazonaws.services.apigatewaymanagementapi.model.PostToConnectionRequest
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.Inject
import com.nimbusframework.nimbuscore.clients.websocket.ServerlessFunctionWebSocketClient
import java.nio.ByteBuffer

internal class ServerlessFunctionWebSocketClientApiGateway: ServerlessFunctionWebSocketClient {

    private val env = System.getenv()
    private val endpoint = if (env.containsKey("WEBSOCKET_ENDPOINT")) env["WEBSOCKET_ENDPOINT"]!! else ""
    private val region = if (env.containsKey("AWS_DEFAULT_REGION")) env["AWS_DEFAULT_REGION"]!! else ""

    @Inject
    private lateinit var clientBuilder: AmazonApiGatewayManagementApiClientBuilder

    private val apiGatewayManagementApi: AmazonApiGatewayManagementApi by lazy {
        clientBuilder
            .withEndpointConfiguration(
                    AwsClientBuilder.EndpointConfiguration(
                            endpoint,
                            region
                    )
            ).build()
    }

    private val objectMapper = ObjectMapper()

    override fun sendToConnection(connectionId: String, data: ByteBuffer) {
        val postToConnectionRequest = PostToConnectionRequest()
                .withConnectionId(connectionId)
                .withData(data)

        apiGatewayManagementApi.postToConnection(postToConnectionRequest)
    }

    override fun sendToConnectionConvertToJson(connectionId: String, data: Any) {
        val json = objectMapper.writeValueAsBytes(data)

        val buffer = ByteBuffer.wrap(json)

        val postToConnectionRequest = PostToConnectionRequest()
                .withConnectionId(connectionId)
                .withData(buffer)
        apiGatewayManagementApi.postToConnection(postToConnectionRequest)
    }
}