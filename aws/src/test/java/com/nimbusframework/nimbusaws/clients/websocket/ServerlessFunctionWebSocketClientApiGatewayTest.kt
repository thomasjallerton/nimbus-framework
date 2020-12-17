package com.nimbusframework.nimbusaws.clients.websocket

import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApi
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApiClientBuilder
import com.amazonaws.services.apigatewaymanagementapi.model.PostToConnectionRequest
import com.amazonaws.services.apigatewaymanagementapi.model.PostToConnectionResult
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.nimbusframework.nimbusaws.examples.SimpleObject
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import java.nio.ByteBuffer

class ServerlessFunctionWebSocketClientApiGatewayTest : AnnotationSpec() {

    private lateinit var underTest: ServerlessFunctionWebSocketClientApiGateway
    private lateinit var managementApi: AmazonApiGatewayManagementApi

    @BeforeEach
    fun setup() {
        underTest = ServerlessFunctionWebSocketClientApiGateway()
        val clientBuilder: AmazonApiGatewayManagementApiClientBuilder = mockk()
        managementApi = mockk()

        every { clientBuilder.withEndpointConfiguration(any()) } returns clientBuilder
        every { clientBuilder.build() } returns managementApi

        val injector = Guice.createInjector(object: AbstractModule() {
            override fun configure() {
                bind(AmazonApiGatewayManagementApiClientBuilder::class.java).toInstance(clientBuilder)
            }
        })
        injector.injectMembers(underTest)
    }

    @Test
    fun canSendToConnection() {
        val request = slot<PostToConnectionRequest>()
        val byteBuffer = ByteBuffer.wrap("hello".toByteArray())

        every { managementApi.postToConnection(capture(request)) } returns PostToConnectionResult()

        underTest.sendToConnection("connectionId", byteBuffer)

        request.captured.data shouldBe byteBuffer
        request.captured.connectionId shouldBe "connectionId"
    }

    @Test
    fun canSendToConnectionJson() {
        val request = slot<PostToConnectionRequest>()

        val objectMapper = ObjectMapper()
        val obj = SimpleObject("test")

        val byteBuffer = ByteBuffer.wrap(objectMapper.writeValueAsBytes(obj))

        every { managementApi.postToConnection(capture(request)) } returns PostToConnectionResult()

        underTest.sendToConnectionConvertToJson("connectionId", obj)

        request.captured.data shouldBe byteBuffer
        request.captured.connectionId shouldBe "connectionId"
    }



}