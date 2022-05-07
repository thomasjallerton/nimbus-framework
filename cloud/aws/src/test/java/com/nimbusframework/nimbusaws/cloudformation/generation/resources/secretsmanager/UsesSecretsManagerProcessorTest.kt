package com.nimbusframework.nimbusaws.cloudformation.generation.resources.secretsmanager

import com.google.testing.compile.Compilation
import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.annotation.processor.ProcessingData
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.apigateway.HttpFunctionResourceCreator
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.cognito.UsesCognitoProcessor
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

internal class UsesSecretsManagerProcessorTest: AnnotationSpec() {

    private lateinit var usesSecretsManagerProcessor: UsesSecretsManagerProcessor
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

        compileStateService = CompileStateService("handlers/secrets/UsesSecretsHandler.java")
    }

    private fun setup(processingEnvironment: ProcessingEnvironment, toRun: () -> Unit ) {
        val elements = processingEnvironment.elementUtils
        usesSecretsManagerProcessor = UsesSecretsManagerProcessor(nimbusState)

        every { messager.printMessage(Diagnostic.Kind.ERROR, any(), any()) } answers { processingEnvironment.messager.printMessage(Diagnostic.Kind.ERROR, "") }

        HttpFunctionResourceCreator(cfDocuments, processingData, mockk(relaxed = true), processingEnvironment, setOf(), mockk(relaxed = true)).handleElement(elements.getTypeElement("handlers.secrets.UsesSecretsHandler").enclosedElements[1], FunctionEnvironmentService(cfDocuments, processingData))

        toRun()
    }

    @Test
    fun correctlySetsPermissions() {
        compileStateService.compileObjects {
            setup(it) {
                val iamRoleResource = cfDocuments["dev"]!!.updateTemplate.resources.get("IamRoleesSecretsHandlerfunc") as IamRoleResource

                val functionResource = cfDocuments["dev"]!!.updateTemplate.resources.get("UsesSecretsHandlerfuncFunction") as FunctionResource

                usesSecretsManagerProcessor.handleUseResources(it.elementUtils.getTypeElement("handlers.secrets.UsesSecretsHandler").enclosedElements[1], functionResource)

                iamRoleResource.allows("secretsmanager:GetSecretValue", ExistingResource("arn:partition:service:region:account-id:resource-id", nimbusState, "dev")) shouldBe true
            }
        }
        compileStateService.status shouldBe Compilation.Status.SUCCESS
    }

}

