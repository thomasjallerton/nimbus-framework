package services

import org.apache.maven.plugin.logging.Log
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.lambda.LambdaClient
import software.amazon.awssdk.services.lambda.model.InvocationType
import software.amazon.awssdk.services.lambda.model.InvokeRequest

class LambdaService(private val logger: Log, region: Region) {
    private val lambdaClient = LambdaClient.builder()
        .region(region)
        .build()

    fun invokeNoArgs(functionName: String) {
        val invokeRequest = InvokeRequest.builder()
            .functionName(functionName)
            .invocationType(InvocationType.REQUEST_RESPONSE)
            .build()
        val result = lambdaClient.invoke(invokeRequest)
        logger.info("Function returned value: " + String(result.payload().asByteArray()))
    }
}
