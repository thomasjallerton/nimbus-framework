package com.nimbusframework.nimbusaws.cloudformation.generation.resources.apigateway

import com.amazonaws.services.lambda.runtime.events.APIGatewayCustomAuthorizerEvent
import com.amazonaws.services.lambda.runtime.events.IamPolicyResponse
import com.nimbusframework.nimbusaws.annotation.annotations.apigateway.ApiGatewayRestConfig
import com.nimbusframework.nimbusaws.annotation.annotations.apigateway.ApiGatewayRestConfigs
import com.nimbusframework.nimbusaws.annotation.annotations.cognito.ExistingCognitoUserPool
import com.nimbusframework.nimbusaws.annotation.processor.AwsMethodInformation
import com.nimbusframework.nimbusaws.annotation.processor.FunctionInformation
import com.nimbusframework.nimbusaws.annotation.processor.ProcessingData
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.FunctionResourceCreator
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.CloudResourceResourceCreator
import com.nimbusframework.nimbusaws.cloudformation.model.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.model.resource.function.FunctionConfig
import com.nimbusframework.nimbusaws.cloudformation.model.resource.function.FunctionPermissionResource
import com.nimbusframework.nimbusaws.cloudformation.model.resource.http.authorizer.CognitoRestApiAuthorizer
import com.nimbusframework.nimbusaws.cloudformation.model.resource.http.authorizer.TokenRestApiAuthorizer
import com.nimbusframework.nimbusaws.interfaces.ApiGatewayLambdaAuthorizer
import com.nimbusframework.nimbusaws.lambda.handlers.HandlerProvider
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import com.nimbusframework.nimbuscore.annotations.AnnotationHelper.getAnnotationForStage
import javax.annotation.processing.Messager
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

class ApiGatewayConfigResourceCreator(
    roundEnvironment: RoundEnvironment,
    cfDocuments: MutableMap<String, CloudFormationFiles>,
    private val processingData: ProcessingData,
    private val processingEnvironment: ProcessingEnvironment,
    private val messager: Messager,
    private val functionEnvironmentService: FunctionEnvironmentService
) : CloudResourceResourceCreator(
    roundEnvironment,
    cfDocuments,
    processingData.nimbusState,
    ApiGatewayRestConfig::class.java,
    ApiGatewayRestConfigs::class.java
) {

    override fun handleAgnosticType(type: Element) {
        val restConfigs = type.getAnnotationsByType(ApiGatewayRestConfig::class.java)

        for (restConfig in restConfigs) {
            for (stage in stageService.determineStages(restConfig.stages)) {
                val authorizerClass = ApiGatewayRestConfigAnnotation(restConfig).getTypeElement(processingEnvironment)

                val cloudFormationDocuments =
                    cfDocuments.getOrPut(stage) { CloudFormationFiles(nimbusState, stage) }

                val userPoolAnnotation =
                    getAnnotationForStage(authorizerClass, ExistingCognitoUserPool::class, stage) { it.stages }
                val isInterface =
                    authorizerClass.interfaces.any { it.toString() == ApiGatewayLambdaAuthorizer::class.qualifiedName }

                if (userPoolAnnotation != null && isInterface) {
                    messager.printMessage(
                        Diagnostic.Kind.ERROR,
                        "${authorizerClass.qualifiedName} cannot be annotated with @ExistingCognitoUserPool and implement " +
                                "${ApiGatewayLambdaAuthorizer::class.simpleName}, just use one method."
                    )
                }

                if (userPoolAnnotation != null) {
                    handleExistingUserPoolAnnotations(restConfig, userPoolAnnotation, cloudFormationDocuments)
                }

                if (isInterface) {
                    handleFunctionAuthorizer(restConfig, authorizerClass, cloudFormationDocuments)
                }
            }
        }
    }

    private fun handleExistingUserPoolAnnotations(config: ApiGatewayRestConfig, userPoolAnnotation: ExistingCognitoUserPool, cloudFormationDocuments: CloudFormationFiles) {
        val restApi = cloudFormationDocuments.updateTemplate.getOrCreateRootRestApi()

        val authorizer = CognitoRestApiAuthorizer(userPoolAnnotation.arn, config.authorizationHeader, restApi, config.authorizationCacheTtl, nimbusState, restApi.stage)
        cloudFormationDocuments.updateTemplate.addRestApiAuthorizer(authorizer)
    }

    private fun handleFunctionAuthorizer(config: ApiGatewayRestConfig, authorizerClass: TypeElement, cloudFormationDocuments: CloudFormationFiles) {
        val restApi = cloudFormationDocuments.updateTemplate.getOrCreateRootRestApi()

        val function = authorizerClass.enclosedElements.first { it.kind == ElementKind.METHOD && it.simpleName.toString() == "handleRequest" }

        val methodInformation = FunctionResourceCreator.extractMethodInformation(function, processingEnvironment, messager)
        val handlerInformation = FunctionResourceCreator.createHandlerInformation(function, HandlerProvider(
            authorizerClass.qualifiedName.toString(),
            authorizerClass.qualifiedName.toString() + "::" + function.simpleName.toString()
        ))

        val functionResource = functionEnvironmentService.newFunction(methodInformation, handlerInformation, FunctionConfig(10, 1024, restApi.stage))

        nimbusState.handlerFiles.add(handlerInformation)

        val authorizer = TokenRestApiAuthorizer(functionResource, config.authorizationHeader, restApi, config.authorizationCacheTtl, nimbusState, restApi.stage)

        val permission = FunctionPermissionResource(functionResource, authorizer, nimbusState)

        cloudFormationDocuments.updateTemplate.addRestApiAuthorizer(authorizer)
        cloudFormationDocuments.updateTemplate.resources.addResource(permission)

        val awsFunctionInformation = if (handlerInformation.isCustomFunction()) {
            null
        } else {
            AwsMethodInformation(
                methodInformation.packageName,
                authorizerClass.simpleName.toString(),
                APIGatewayCustomAuthorizerEvent::class.qualifiedName!!,
                IamPolicyResponse::class.qualifiedName!!
            )
        }
        val functionInformation = FunctionInformation(function, functionResource, awsFunctionInformation, false)
        processingData.functions.add(functionInformation)
    }
}
