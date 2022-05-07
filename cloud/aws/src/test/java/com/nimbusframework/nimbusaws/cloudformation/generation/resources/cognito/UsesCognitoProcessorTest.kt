package com.nimbusframework.nimbusaws.cloudformation.generation.resources.cognito

import com.google.testing.compile.Compilation
import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.annotation.processor.ProcessingData
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.apigateway.HttpFunctionResourceCreator
import com.nimbusframework.nimbusaws.cloudformation.model.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.model.resource.ExistingResource
import com.nimbusframework.nimbusaws.cloudformation.model.resource.IamRoleResource
import com.nimbusframework.nimbusaws.cloudformation.model.resource.function.FunctionResource
import com.nimbusframework.nimbuscore.persisted.NimbusState
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.tools.Diagnostic

internal class UsesCognitoProcessorTest: AnnotationSpec() {

    private lateinit var usesCognitoProcessor: UsesCognitoProcessor
    private lateinit var roundEnvironment: RoundEnvironment
    private lateinit var cfDocuments: MutableMap<String, CloudFormationFiles>
    private lateinit var nimbusState: NimbusState
    private lateinit var processingData: ProcessingData
    private lateinit var messager: Messager
    private lateinit var compileStateService: CompileStateService

    @BeforeEach
    fun setup() {
        nimbusState = NimbusState(defaultStages = listOf("dev"))
        processingData = ProcessingData(nimbusState)
        cfDocuments = mutableMapOf()
        roundEnvironment = mockk()
        messager = mockk()

        compileStateService = CompileStateService("models/cognito/UserPool.java", "handlers/cognito/UsesCognitoUserPoolHandler.java")
    }

    private fun setup(processingEnvironment: ProcessingEnvironment, toRun: () -> Unit ) {
        val elements = processingEnvironment.elementUtils
        usesCognitoProcessor = UsesCognitoProcessor(cfDocuments, processingEnvironment, messager, processingData.nimbusState)

        every { messager.printMessage(Diagnostic.Kind.ERROR, any(), any()) } answers { processingEnvironment.messager.printMessage(Diagnostic.Kind.ERROR, "") }

        ExistingCognitoResourceCreator(roundEnvironment, cfDocuments, nimbusState).handleAgnosticType(elements.getTypeElement("models.cognito.UserPool"))

        HttpFunctionResourceCreator(cfDocuments, processingData, mockk(relaxed = true), processingEnvironment, setOf(), mockk(relaxed = true)).handleElement(elements.getTypeElement("handlers.cognito.UsesCognitoUserPoolHandler").enclosedElements[1], FunctionEnvironmentService(cfDocuments, processingData))
        HttpFunctionResourceCreator(cfDocuments, processingData, mockk(relaxed = true), processingEnvironment, setOf(), mockk(relaxed = true)).handleElement(elements.getTypeElement("handlers.cognito.UsesCognitoUserPoolHandler").enclosedElements[2], FunctionEnvironmentService(cfDocuments, processingData))

        toRun()
    }

    @Test
    fun correctlySetsPermissions() {
        compileStateService.compileObjects {
            setup(it) {
                val functionResource = cfDocuments["dev"]!!.updateTemplate.resources.get("UsesCognitoUserPoolHandlerfuncFunction") as FunctionResource
                val iamRoleResource = functionResource.iamRoleResource


                usesCognitoProcessor.handleUseResources(it.elementUtils.getTypeElement("handlers.cognito.UsesCognitoUserPoolHandler").enclosedElements[1], functionResource)

                iamRoleResource.allows("cognito-idp:GetUser", ExistingResource("arn:partition:service:region:account-id:resource-id", nimbusState, "dev")) shouldBe true
                iamRoleResource.allows("cognito-idp:ListUsers", ExistingResource("arn:partition:service:region:account-id:resource-id", nimbusState, "dev")) shouldBe true
            }
        }
        compileStateService.status shouldBe Compilation.Status.SUCCESS
    }

    @Test
    fun correctlySetsPermissionsForAdminUse() {
        compileStateService.compileObjects {
            setup(it) {
                val functionResource = cfDocuments["dev"]!!.updateTemplate.resources.get("UsesCognitoUserPoolHandlerfunc2Function") as FunctionResource
                val iamRoleResource = functionResource.iamRoleResource

                usesCognitoProcessor.handleUseResources(it.elementUtils.getTypeElement("handlers.cognito.UsesCognitoUserPoolHandler").enclosedElements[2], functionResource)

                iamRoleResource.allows("cognito-idp:AdminAddUserToGroup", ExistingResource("arn:partition:service:region:account-id:resource-id", nimbusState, "dev")) shouldBe true
                iamRoleResource.allows("cognito-idp:AdminRemoveUserFromGroup", ExistingResource("arn:partition:service:region:account-id:resource-id", nimbusState, "dev")) shouldBe true
                iamRoleResource.allows("cognito-idp:AdminUpdateUserAttributes", ExistingResource("arn:partition:service:region:account-id:resource-id", nimbusState, "dev")) shouldBe true
            }
        }
        compileStateService.status shouldBe Compilation.Status.SUCCESS
    }
}
