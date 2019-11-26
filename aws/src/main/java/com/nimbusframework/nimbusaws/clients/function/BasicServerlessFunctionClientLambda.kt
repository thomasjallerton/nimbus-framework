package com.nimbusframework.nimbusaws.clients.function

import com.amazonaws.services.lambda.AWSLambda
import com.nimbusframework.nimbusaws.cloudformation.resource.function.FunctionResource
import com.amazonaws.services.lambda.AWSLambdaClientBuilder
import com.amazonaws.services.lambda.model.InvocationType
import com.amazonaws.services.lambda.model.InvokeRequest
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.Inject
import com.nimbusframework.nimbuscore.clients.function.BasicServerlessFunctionClient
import java.nio.charset.Charset


internal class BasicServerlessFunctionClientLambda(
        private val handlerClass: Class<out Any>,
        private val functionName: String
): BasicServerlessFunctionClient {

    @Inject
    private lateinit var lambdaClient: AWSLambda

    private val objectMapper = ObjectMapper()

    private val projectName = if (System.getenv().containsKey("NIMBUS_PROJECT_NAME")) {
        System.getenv("NIMBUS_PROJECT_NAME")
    } else {
        ""
    }

    private val stage = if (System.getenv().containsKey("FUNCTION_STAGE")) {
        System.getenv("FUNCTION_STAGE")
    } else {
        "dev"
    }

    override fun invoke() {
        invoke("", Unit.javaClass)
    }

    override fun invoke(param: Any) {
        invoke(param, Unit.javaClass)
    }

    override fun <T> invoke(responseType: Class<T>): T? {
        return invoke("", responseType)
    }

    override fun <T> invoke(param: Any, responseType: Class<T>): T? {
        val invokeRequest = InvokeRequest()
                .withFunctionName(FunctionResource.functionName(projectName, handlerClass.simpleName, functionName, stage))
                .withPayload(objectMapper.writeValueAsString(param))
                .withInvocationType(InvocationType.RequestResponse)
        val result = lambdaClient.invoke(invokeRequest)
        return if (responseType != Unit.javaClass) {
            val converted = String(result.payload.array(), Charset.forName("UTF-8"))
            objectMapper.readValue(converted, responseType)
        } else {
            null
        }
    }

    override fun invokeAsync() {
        invokeAsync("")
    }

    override fun invokeAsync(param: Any) {
        val invokeRequest = InvokeRequest()
                .withFunctionName(FunctionResource.functionName(projectName, handlerClass.simpleName, functionName, stage))
                .withPayload(objectMapper.writeValueAsString(param))
                .withInvocationType(InvocationType.Event)
        lambdaClient.invoke(invokeRequest)
    }
}