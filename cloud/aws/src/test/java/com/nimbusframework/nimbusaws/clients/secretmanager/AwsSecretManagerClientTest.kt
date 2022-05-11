package com.nimbusframework.nimbusaws.clients.secretmanager

import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import software.amazon.awssdk.services.cognitoidentityprovider.model.GetUserRequest
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse
import java.util.function.Consumer

class AwsSecretManagerClientTest : AnnotationSpec() {

    private lateinit var underTest: AwsSecretManagerClient
    private lateinit var awsClient: SecretsManagerClient

    @BeforeEach
    fun setup() {
        awsClient = mockk(relaxed = true)
        underTest = AwsSecretManagerClient(awsClient)
    }

    @Test
    fun canGetSecret() {
        val secretResponse = GetSecretValueResponse.builder()
            .secretString("VALUE")
            .build()
        val getSecretValueRequest = slot<Consumer<GetSecretValueRequest.Builder>>()
        every { awsClient.getSecretValue(capture(getSecretValueRequest)) } returns secretResponse

        underTest.getSecret("NAME") shouldBe "VALUE"

        val builder = GetSecretValueRequest.builder()
        getSecretValueRequest.captured.accept(builder)

        val built = builder.build()
        built.secretId() shouldBe "NAME"
    }

}
