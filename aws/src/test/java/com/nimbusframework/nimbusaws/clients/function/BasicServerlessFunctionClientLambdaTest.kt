package com.nimbusframework.nimbusaws.clients.function

import com.amazonaws.services.lambda.AWSLambda
import com.amazonaws.services.lambda.model.InvocationType
import com.amazonaws.services.lambda.model.InvokeRequest
import com.amazonaws.services.lambda.model.InvokeResult
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.nimbusframework.nimbusaws.cloudformation.resource.function.FunctionResource
import com.nimbusframework.nimbusaws.examples.BasicFunctionHandler
import com.nimbusframework.nimbuscore.clients.function.EnvironmentVariableClient
import io.kotlintest.matchers.types.shouldBeSameInstanceAs
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.AnnotationSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.nio.charset.StandardCharsets.UTF_8

class BasicServerlessFunctionClientLambdaTest : AnnotationSpec() {

    private lateinit var underTest: BasicServerlessFunctionClientLambda
    private lateinit var awsLambda: AWSLambda

    private lateinit var environmentVariableClient: EnvironmentVariableClient

    @BeforeEach
    fun setup() {
        underTest = BasicServerlessFunctionClientLambda(BasicFunctionHandler::class.java, "exampleFunc")
        awsLambda = mockk(relaxed = true)
        environmentVariableClient = mockk()
        val injector = Guice.createInjector(object: AbstractModule() {
            override fun configure() {
                bind(AWSLambda::class.java).toInstance(awsLambda)
                bind(EnvironmentVariableClient::class.java).toInstance(environmentVariableClient)
            }
        })
        injector.injectMembers(underTest)
        every { environmentVariableClient.get("NIMBUS_PROJECT_NAME") } returns "PROJECT"
        every { environmentVariableClient.get("FUNCTION_STAGE") } returns "STAGE"
    }

    @Test
    fun canInvokeWithNoParamsAndNoResponse() {
        val invokeResult = slot<InvokeRequest>()
        every { awsLambda.invoke(capture(invokeResult)) } returns InvokeResult()

        underTest.invoke()
        invokeResult.captured.functionName shouldBe FunctionResource.functionName("PROJECT", "BasicFunctionHandler", "exampleFunc", "STAGE")
        UTF_8.decode(invokeResult.captured.payload).toString() shouldBe "\"\""
        invokeResult.captured.invocationType shouldBe InvocationType.RequestResponse.toString()
    }

    @Test
    fun canInvokeWithParamsAndNoResponse() {
        val invokeResult = slot<InvokeRequest>()
        every { awsLambda.invoke(capture(invokeResult)) } returns InvokeResult()

        underTest.invoke("TEST")
        UTF_8.decode(invokeResult.captured.payload).toString() shouldBe "\"TEST\""
    }

    @Test
    fun canInvokeWithNoParamsAndResponse() {
        val invokeResult = slot<InvokeRequest>()
        every { awsLambda.invoke(capture(invokeResult)) } returns InvokeResult().withPayload(ByteBuffer.wrap("\"TEST\"".toByteArray()))

        underTest.invoke(String::class.java) shouldBe "TEST"
        invokeResult.captured.functionName shouldBe FunctionResource.functionName("PROJECT", "BasicFunctionHandler", "exampleFunc", "STAGE")
    }

    @Test
    fun canInvokeAsyncWithParams() {
        val invokeResult = slot<InvokeRequest>()
        every { awsLambda.invoke(capture(invokeResult)) } returns InvokeResult()

        underTest.invokeAsync("TEST")

        invokeResult.captured.invocationType shouldBe InvocationType.Event.toString()
        UTF_8.decode(invokeResult.captured.payload).toString() shouldBe "\"TEST\""
    }

    @Test
    fun canInvokeAsyncWithNoParams() {
        val invokeResult = slot<InvokeRequest>()
        every { awsLambda.invoke(capture(invokeResult)) } returns InvokeResult()

        underTest.invokeAsync()

        invokeResult.captured.invocationType shouldBe InvocationType.Event.toString()
    }
}