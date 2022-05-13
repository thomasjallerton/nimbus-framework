package services

import com.amazonaws.services.lambda.AWSLambdaClientBuilder
import com.amazonaws.services.lambda.model.InvocationType
import com.amazonaws.services.lambda.model.InvokeRequest
import org.apache.maven.plugin.logging.Log

class LambdaService(private val logger: Log, region: String) {
    private val lambdaClient = AWSLambdaClientBuilder.standard()
            .withRegion(region)
            .build()

    fun invokeNoArgs(functionName: String) {
        val invokeRequest = InvokeRequest()
                .withFunctionName(functionName)
                .withInvocationType(InvocationType.RequestResponse)
        val result = lambdaClient.invoke(invokeRequest)
        logger.info("Function returned value: " + String(result.payload.array()))
    }
}