package com.nimbusframework.nimbusaws.cloudformation.generation.resources.apigateway

import com.amazonaws.services.lambda.runtime.events.APIGatewayCustomAuthorizerEvent
import com.amazonaws.services.lambda.runtime.events.IamPolicyResponse
import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.annotation.processor.AwsMethodInformation
import com.nimbusframework.nimbusaws.annotation.processor.ProcessingData
import com.nimbusframework.nimbusaws.cloudformation.generation.FunctionAssertions.functionShouldBeSetUpCorrectly
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.cloudformation.model.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.model.resource.function.FunctionPermissionResource
import com.nimbusframework.nimbusaws.cloudformation.model.resource.http.authorizer.CognitoRestApiAuthorizer
import com.nimbusframework.nimbusaws.cloudformation.model.resource.http.authorizer.TokenRestApiAuthorizer
import com.nimbusframework.nimbuscore.persisted.NimbusState
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.mockk
import javax.annotation.processing.RoundEnvironment
import io.kotest.matchers.types.shouldBeInstanceOf

internal class ApiGatewayConfigResourceCreatorTest: AnnotationSpec() {

    private lateinit var roundEnvironment: RoundEnvironment
    private lateinit var cfDocuments: MutableMap<String, CloudFormationFiles>
    private lateinit var processingData: ProcessingData
    private lateinit var functionEnvironmentService: FunctionEnvironmentService

    @BeforeEach
    fun setup() {
        processingData = ProcessingData(NimbusState(customRuntime = true))
        cfDocuments = mutableMapOf()
        roundEnvironment = mockk()
        functionEnvironmentService = FunctionEnvironmentService(cfDocuments, processingData)
    }

    @Test
    fun correctlySetsUpLambdaAuthorizer() {
        val compileStateService = CompileStateService("models/apigateway/ConfiguredApiGatewayLambdaHandler.java", "handlers/apigateway/Authorizer.java")
        compileStateService.compileObjects {
            val apiGatewayConfigResourceCreator = ApiGatewayConfigResourceCreator(roundEnvironment, cfDocuments, processingData, it, it.messager, functionEnvironmentService)

            val restApiTypeElem = it.elementUtils.getTypeElement("models.apigateway.ConfiguredApiGatewayLambdaHandler")
            apiGatewayConfigResourceCreator.handleAgnosticType(restApiTypeElem)

            cfDocuments["dev"] shouldNotBe null
            val resources = cfDocuments["dev"]!!.updateTemplate.resources

            // then
            // ... created function
            val functionTypeElem = it.elementUtils.getTypeElement("handlers.apigateway.Authorizer").enclosedElements[1]
            val functionResource = resources.getFunction("handlers.apigateway.Authorizer", "handleRequest")!!

            functionShouldBeSetUpCorrectly(functionResource, functionTypeElem, processingData)

            // ... created authorizer and permission
            val authorizer = cfDocuments["dev"]!!.updateTemplate.getRestApiAuthorizer()!!
            authorizer.shouldBeInstanceOf<TokenRestApiAuthorizer>()
            authorizer.function shouldBe functionResource
            authorizer.identityHeader shouldBe "Authorization"
            authorizer.ttlSeconds shouldBe 300

            val permission = resources.get("LambdaPermRestAuth" + functionResource.getShortName())!! as FunctionPermissionResource
            permission.trigger shouldBe authorizer

            val functionInformation = processingData.functions.first { it.resource == functionResource }
            functionInformation.awsMethodInformation shouldBe AwsMethodInformation(
                "handlers.apigateway",
                "Authorizer",
                APIGatewayCustomAuthorizerEvent::class.qualifiedName!!,
                IamPolicyResponse::class.qualifiedName!!
            )

        }
    }

    @Test
    fun correctlySetsUpCustomLambdaAuthorizer() {
        val compileStateService = CompileStateService("models/apigateway/ConfiguredApiGatewayCustomHandler.java", "handlers/apigateway/CustomAuthorizer.java")
        compileStateService.compileObjects {
            val apiGatewayConfigResourceCreator = ApiGatewayConfigResourceCreator(roundEnvironment, cfDocuments, processingData, it, it.messager, functionEnvironmentService)

            val restApiTypeElem = it.elementUtils.getTypeElement("models.apigateway.ConfiguredApiGatewayCustomHandler")
            apiGatewayConfigResourceCreator.handleAgnosticType(restApiTypeElem)

            cfDocuments["dev"] shouldNotBe null
            val resources = cfDocuments["dev"]!!.updateTemplate.resources

            // then
            // ... created function
            val functionTypeElem = it.elementUtils.getTypeElement("handlers.apigateway.Authorizer").enclosedElements[1]
            val functionResource = resources.getFunction("handlers.apigateway.Authorizer", "handleRequest")!!

            functionShouldBeSetUpCorrectly(functionResource, functionTypeElem, processingData)

            // ... created authorizer and permission
            val authorizer = cfDocuments["dev"]!!.updateTemplate.getRestApiAuthorizer()!!
            authorizer.shouldBeInstanceOf<TokenRestApiAuthorizer>()
            authorizer.function shouldBe functionResource
            authorizer.identityHeader shouldBe "Bearer"
            authorizer.ttlSeconds shouldBe 100

            val permission = resources.get("LambdaPermRestAuth" + functionResource.getShortName())!! as FunctionPermissionResource
            permission.trigger shouldBe authorizer

            val functionInformation = processingData.functions.first { it.resource == functionResource }
            functionInformation.awsMethodInformation shouldBe null
        }
    }

    @Test
    fun correctlySetsUpCognitoAuthorizer() {
        val compileStateService = CompileStateService("models/apigateway/ConfiguredApiGatewayCognito.java", "models/cognito/UserPool.java")
        compileStateService.compileObjects {
            val apiGatewayConfigResourceCreator = ApiGatewayConfigResourceCreator(roundEnvironment, cfDocuments, processingData, it, it.messager, functionEnvironmentService)

            val restApiTypeElem = it.elementUtils.getTypeElement("models.apigateway.ConfiguredApiGatewayCustomHandler")
            apiGatewayConfigResourceCreator.handleAgnosticType(restApiTypeElem)

            cfDocuments["dev"] shouldNotBe null

            // then
            // ... created authorizer
            val authorizer = cfDocuments["dev"]!!.updateTemplate.getRestApiAuthorizer()!!
            authorizer.shouldBeInstanceOf<CognitoRestApiAuthorizer>()
            authorizer.cognitoUserPoolArn shouldBe "arn:partition:service:region:account-id:resource-id"
            authorizer.cognitoUserPoolArn shouldBe "Authorization"
            authorizer.ttlSeconds shouldBe 300
        }
    }

}
