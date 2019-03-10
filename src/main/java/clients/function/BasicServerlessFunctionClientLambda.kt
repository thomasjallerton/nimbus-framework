package clients.function

import cloudformation.resource.function.FunctionResource
import com.amazonaws.services.lambda.AWSLambdaClientBuilder
import com.amazonaws.services.lambda.model.InvocationType
import com.amazonaws.services.lambda.model.InvokeRequest
import com.fasterxml.jackson.databind.ObjectMapper
import java.nio.charset.Charset


internal class BasicServerlessFunctionClientLambda: BasicServerlessFunctionClient {

    private val lambdaClient = AWSLambdaClientBuilder.defaultClient()
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


        override fun invoke(handlerClass: Class<out Any>, functionName: String) {
        invoke(handlerClass, functionName, "", Unit.javaClass)
    }

    override fun invoke(handlerClass: Class<out Any>, functionName: String, param: Any) {
        invoke(handlerClass, functionName, param, Unit.javaClass)
    }

    override fun <T> invoke(handlerClass: Class<out Any>, functionName: String, param: Any, responseType: Class<T>): T? {
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

    override fun invokeAsync(handlerClass: Class<out Any>, functionName: String) {
        invokeAsync(handlerClass, functionName, "")
    }

    override fun invokeAsync(handlerClass: Class<out Any>, functionName: String, param: Any) {
        val invokeRequest = InvokeRequest()
                .withFunctionName(FunctionResource.functionName(projectName, handlerClass.simpleName, functionName, stage))
                .withPayload(objectMapper.writeValueAsString(param))
                .withInvocationType(InvocationType.Event)
        lambdaClient.invoke(invokeRequest)
    }
}