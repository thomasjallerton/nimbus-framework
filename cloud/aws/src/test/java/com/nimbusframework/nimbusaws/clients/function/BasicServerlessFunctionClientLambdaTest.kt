package com.nimbusframework.nimbusaws.clients.function

import com.nimbusframework.nimbusaws.clients.InternalEnvironmentVariableClient
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.environment.ConstantEnvironmentVariable
import com.nimbusframework.nimbusaws.cloudformation.model.resource.function.FunctionResource
import com.nimbusframework.nimbusaws.examples.BasicFunctionHandler
import com.nimbusframework.nimbuscore.clients.function.EnvironmentVariableClient
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.services.lambda.LambdaClient
import software.amazon.awssdk.services.lambda.model.InvocationType
import software.amazon.awssdk.services.lambda.model.InvokeRequest
import software.amazon.awssdk.services.lambda.model.InvokeResponse
import java.nio.charset.StandardCharsets.UTF_8

class BasicServerlessFunctionClientLambdaTest : AnnotationSpec() {

    private lateinit var underTest: BasicServerlessFunctionClientLambda
    private lateinit var awsLambda: LambdaClient

    private lateinit var environmentVariableClient: InternalEnvironmentVariableClient

    @BeforeEach
    fun setup() {
        awsLambda = mockk(relaxed = true)
        environmentVariableClient = mockk()
        every { environmentVariableClient.get(ConstantEnvironmentVariable.NIMBUS_PROJECT_NAME) } returns "PROJECT"
        every { environmentVariableClient.get(ConstantEnvironmentVariable.FUNCTION_STAGE) } returns "STAGE"
        underTest = BasicServerlessFunctionClientLambda(BasicFunctionHandler::class.java, "exampleFunc", awsLambda, environmentVariableClient)
    }

    @Test
    fun canInvokeWithNoParamsAndNoResponse() {
        val invokeResult = slot<InvokeRequest>()
        every { awsLambda.invoke(capture(invokeResult)) } returns InvokeResponse.builder().build()

        underTest.invoke()
        invokeResult.captured.functionName() shouldBe FunctionResource.functionName("PROJECT", "com.nimbusframework.nimbusaws.examples", "BasicFunctionHandler", "exampleFunc", "STAGE")
        UTF_8.decode(invokeResult.captured.payload().asByteBuffer()).toString() shouldBe "\"\""
        invokeResult.captured.invocationType() shouldBe InvocationType.REQUEST_RESPONSE
    }

    @Test
    fun canInvokeWithParamsAndNoResponse() {
        val invokeResult = slot<InvokeRequest>()
        every { awsLambda.invoke(capture(invokeResult)) } returns InvokeResponse.builder().build()

        underTest.invoke("TEST")
        UTF_8.decode(invokeResult.captured.payload().asByteBuffer()).toString() shouldBe "\"TEST\""
    }

    @Test
    fun canInvokeWithNoParamsAndResponse() {
        val invokeResult = slot<InvokeRequest>()
        every { awsLambda.invoke(capture(invokeResult)) } returns InvokeResponse.builder().payload(SdkBytes.fromByteArray("\"TEST\"".toByteArray())).build()

        underTest.invoke(String::class.java) shouldBe "TEST"
        invokeResult.captured.functionName() shouldBe FunctionResource.functionName("PROJECT", "com.nimbusframework.nimbusaws.examples", "BasicFunctionHandler", "exampleFunc", "STAGE")
    }

    @Test
    fun canInvokeAsyncWithParams() {
        val invokeResult = slot<InvokeRequest>()
        every { awsLambda.invoke(capture(invokeResult)) } returns InvokeResponse.builder().build()

        underTest.invokeAsync("TEST")

        invokeResult.captured.invocationType() shouldBe InvocationType.EVENT
        UTF_8.decode(invokeResult.captured.payload().asByteBuffer()).toString() shouldBe "\"TEST\""
    }

    @Test
    fun canInvokeAsyncWithNoParams() {
        val invokeResult = slot<InvokeRequest>()
        every { awsLambda.invoke(capture(invokeResult)) } returns InvokeResponse.builder().build()

        underTest.invokeAsync()

        invokeResult.captured.invocationType() shouldBe InvocationType.EVENT
    }
}
