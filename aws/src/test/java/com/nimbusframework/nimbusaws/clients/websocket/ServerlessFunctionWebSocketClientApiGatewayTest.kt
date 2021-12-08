package com.nimbusframework.nimbusaws.clients.websocket

import com.nimbusframework.nimbusaws.examples.SimpleObject
import com.nimbusframework.nimbuscore.clients.JacksonClient
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import software.amazon.awssdk.services.apigatewaymanagementapi.ApiGatewayManagementApiClient
import software.amazon.awssdk.services.apigatewaymanagementapi.ApiGatewayManagementApiClientBuilder
import software.amazon.awssdk.services.apigatewaymanagementapi.model.PostToConnectionRequest
import software.amazon.awssdk.services.apigatewaymanagementapi.model.PostToConnectionResponse
import java.nio.ByteBuffer

class ServerlessFunctionWebSocketClientApiGatewayTest : AnnotationSpec() {

    private lateinit var underTest: ServerlessFunctionWebSocketClientApiGateway
    private lateinit var managementApi: ApiGatewayManagementApiClient

    @BeforeEach
    fun setup() {
        val clientBuilder: ApiGatewayManagementApiClientBuilder = mockk()
        underTest = ServerlessFunctionWebSocketClientApiGateway(clientBuilder)
        managementApi = mockk()

        every { clientBuilder.endpointOverride(any()) } returns clientBuilder
        every { clientBuilder.build() } returns managementApi
    }

    @Test
    fun canSendToConnection() {
        val request = slot<PostToConnectionRequest>()
        val byteBuffer = ByteBuffer.wrap("hello".toByteArray())

        every { managementApi.postToConnection(capture(request)) } returns PostToConnectionResponse.builder().build()

        underTest.sendToConnection("connectionId", byteBuffer)

        request.captured.data().asByteBuffer() shouldBe byteBuffer
        request.captured.connectionId() shouldBe "connectionId"
    }

    @Test
    fun canSendToConnectionJson() {
        val request = slot<PostToConnectionRequest>()

        val obj = SimpleObject("test")

        val byteBuffer = ByteBuffer.wrap(JacksonClient.writeValueAsBytes(obj))

        every { managementApi.postToConnection(capture(request)) } returns PostToConnectionResponse.builder().build()

        underTest.sendToConnectionConvertToJson("connectionId", obj)

        request.captured.data().asByteBuffer() shouldBe byteBuffer
        request.captured.connectionId() shouldBe "connectionId"
    }



}
