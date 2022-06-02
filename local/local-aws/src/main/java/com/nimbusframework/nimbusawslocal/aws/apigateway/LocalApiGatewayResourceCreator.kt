package com.nimbusframework.nimbusawslocal.aws.apigateway

import com.nimbusframework.nimbusaws.annotation.annotations.apigateway.ApiGatewayRestConfig
import com.nimbusframework.nimbusaws.annotation.annotations.cognito.ExistingCognitoUserPool
import com.nimbusframework.nimbusaws.interfaces.ApiGatewayLambdaAuthorizer
import com.nimbusframework.nimbusawslocal.aws.LocalAwsResourceHolder
import com.nimbusframework.nimbuscore.annotations.function.BasicServerlessFunction
import com.nimbusframework.nimbuslocal.deployment.basic.BasicFunction
import com.nimbusframework.nimbuslocal.deployment.function.FunctionIdentifier
import com.nimbusframework.nimbuslocal.deployment.function.ServerlessFunction
import com.nimbusframework.nimbuslocal.deployment.function.information.BasicFunctionInformation
import com.nimbusframework.nimbuslocal.deployment.services.LocalResourceHolder
import com.nimbusframework.nimbuslocal.deployment.services.StageService
import com.nimbusframework.nimbuslocal.deployment.services.resource.LocalCreateResourcesHandler

class LocalApiGatewayResourceCreator(
    private val stageService: StageService,
    private val resourceHolder: LocalAwsResourceHolder,
    private val localResourceHolder: LocalResourceHolder
): LocalCreateResourcesHandler {

    override fun createResource(clazz: Class<*>) {
        val apiGatewayConfig = clazz.getAnnotationsByType(ApiGatewayRestConfig::class.java)

        val annotationForStage = stageService.annotationForStage(apiGatewayConfig) { apiGatewayRestConfig -> apiGatewayRestConfig.stages }
        if (annotationForStage != null) {
            val authorizer = annotationForStage.authorizer.java

            val cognitoAnnotations = authorizer.getAnnotationsByType(ExistingCognitoUserPool::class.java)
            val cognitoAnnotationForStage = stageService.annotationForStage(cognitoAnnotations) { cognitoUserPool -> cognitoUserPool.stages }
            if (cognitoAnnotationForStage != null) {
                localResourceHolder.httpAuthenticator = CognitoHttpMethodAuthenticator(authorizer, annotationForStage.authorizationHeader, annotationForStage.authorizationCacheTtl, resourceHolder)
            }

            if (authorizer.interfaces.contains(ApiGatewayLambdaAuthorizer::class.java)) {
                val functionIdentifier = FunctionIdentifier(authorizer.canonicalName, "handleRequest")
                localResourceHolder.httpAuthenticator = LambdaHttpMethodAuthenticator(
                    functionIdentifier,
                    annotationForStage.authorizationHeader,
                    annotationForStage.authorizationCacheTtl,
                    localResourceHolder
                )
            }
        }
    }

}
