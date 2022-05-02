package com.nimbusframework.nimbusaws.clients.function

import com.nimbusframework.nimbusaws.cloudformation.model.resource.function.FunctionResource
import com.nimbusframework.nimbuscore.annotations.NimbusConstants
import com.nimbusframework.nimbuscore.clients.JacksonClient
import com.nimbusframework.nimbuscore.clients.function.BasicServerlessFunctionClient
import com.nimbusframework.nimbuscore.clients.function.EnvironmentVariableClient
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.services.lambda.LambdaClient
import software.amazon.awssdk.services.lambda.model.InvocationType
import software.amazon.awssdk.services.lambda.model.InvokeRequest
import java.nio.charset.Charset


internal class BasicServerlessFunctionClientLambda(
    private val handlerClass: Class<out Any>,
    private val functionName: String,
    private val lambdaClient: LambdaClient,
    private val environmentVariableClient: EnvironmentVariableClient
) : BasicServerlessFunctionClient {

    private val projectName by lazy { environmentVariableClient.get("NIMBUS_PROJECT_NAME") ?: "" }

    private val stage by lazy { environmentVariableClient.get("FUNCTION_STAGE") ?: NimbusConstants.stage }

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
        val invokeRequest = InvokeRequest.builder()
            .functionName(FunctionResource.functionName(projectName, handlerClass.simpleName, functionName, stage))
            .payload(SdkBytes.fromByteArray(JacksonClient.writeValueAsBytes(param)))
            .invocationType(InvocationType.REQUEST_RESPONSE)
            .build()
        val result = lambdaClient.invoke(invokeRequest)

        return if (responseType != Unit.javaClass) {
            val converted = result.payload().asString(Charset.forName("UTF-8"))
            JacksonClient.readValue(converted, responseType)
        } else {
            null
        }
    }

    override fun invokeAsync() {
        invokeAsync("")
    }

    override fun invokeAsync(param: Any) {
        val invokeRequest = InvokeRequest.builder()
            .functionName(FunctionResource.functionName(projectName, handlerClass.simpleName, functionName, stage))
            .payload(SdkBytes.fromByteArray(JacksonClient.writeValueAsBytes(param)))
            .invocationType(InvocationType.EVENT)
            .build()
        lambdaClient.invoke(invokeRequest)
    }
}
