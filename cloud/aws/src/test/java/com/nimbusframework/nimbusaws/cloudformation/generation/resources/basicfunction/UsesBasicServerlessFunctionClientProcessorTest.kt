package com.nimbusframework.nimbusaws.cloudformation.generation.resources.basicfunction

import com.google.testing.compile.Compilation
import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.annotation.processor.ProcessingData
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.basicfunction.BasicFunctionResourceCreator
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.apigateway.HttpFunctionResourceCreator
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.basicfunction.UsesBasicServerlessFunctionClientProcessor
import com.nimbusframework.nimbusaws.cloudformation.model.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.model.resource.IamRoleResource
import com.nimbusframework.nimbusaws.cloudformation.model.resource.function.FunctionResource
import com.nimbusframework.nimbuscore.persisted.NimbusState
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.tools.Diagnostic

class UsesBasicServerlessFunctionClientProcessorTest: AnnotationSpec() {

    private lateinit var usesBasicServerlessFunctionClientProcessor: UsesBasicServerlessFunctionClientProcessor
    private lateinit var roundEnvironment: RoundEnvironment
    private lateinit var cfDocuments: MutableMap<String, CloudFormationFiles>
    private lateinit var nimbusState: NimbusState
    private lateinit var processingData: ProcessingData
    private lateinit var messager: Messager
    private lateinit var compileStateService: CompileStateService

    @BeforeEach
    fun setup() {
        nimbusState = NimbusState()
        processingData = ProcessingData(nimbusState)
        cfDocuments = mutableMapOf()
        roundEnvironment = mockk()
        messager = mockk()

        compileStateService = CompileStateService("handlers/BasicHandlers.java", "handlers/UsesBasicFunctionHandler.java")
    }

    private fun setup(processingEnvironment: ProcessingEnvironment, toRun: () -> Unit ) {
        val elements = processingEnvironment.elementUtils
        usesBasicServerlessFunctionClientProcessor = UsesBasicServerlessFunctionClientProcessor(cfDocuments, processingEnvironment, processingData.nimbusState, messager)

        every { messager.printMessage(Diagnostic.Kind.ERROR, any(), any()) } answers { processingEnvironment.messager.printMessage(Diagnostic.Kind.ERROR, "") }

        BasicFunctionResourceCreator(cfDocuments, processingData, mockk(relaxed = true), processingEnvironment, setOf(), mockk(relaxed = true)).handleElement(elements.getTypeElement("handlers.BasicHandlers").enclosedElements[1], FunctionEnvironmentService(cfDocuments, processingData))

        HttpFunctionResourceCreator(cfDocuments, processingData, mockk(relaxed = true), processingEnvironment, setOf(), mockk(relaxed = true)).handleElement(elements.getTypeElement("handlers.UsesBasicFunctionHandler").enclosedElements[1], FunctionEnvironmentService(cfDocuments, processingData))
        HttpFunctionResourceCreator(cfDocuments, processingData, mockk(relaxed = true), processingEnvironment, setOf(), mockk(relaxed = true)).handleElement(elements.getTypeElement("handlers.UsesBasicFunctionHandler").enclosedElements[2], FunctionEnvironmentService(cfDocuments, processingData))

        toRun()
    }

    @Test
    fun correctlySetsPermissions() {
        compileStateService.compileObjects {
            setup(it) {
                val functionResource = cfDocuments["dev"]!!.updateTemplate.resources.getFunction("handlers.UsesBasicFunctionHandler", "func")!!

                usesBasicServerlessFunctionClientProcessor.handleUseResources(it.elementUtils.getTypeElement("handlers.UsesBasicFunctionHandler").enclosedElements[1], functionResource)
                val basicFunctionResource = cfDocuments["dev"]!!.updateTemplate.resources.get("hBasicHandlersgetCurrentTimeFunction")!!

                functionResource.iamRoleResource.allows("lambda:*", basicFunctionResource) shouldBe true

                functionResource.getStrEnvValue("NIMBUS_PROJECT_NAME") shouldBe ""
                functionResource.getStrEnvValue("FUNCTION_STAGE") shouldBe "dev"
            }
        }
        compileStateService.status shouldBe Compilation.Status.SUCCESS
    }

    @Test
    fun alertsIfNoCorrespondingBasicFunction() {
        compileStateService.compileObjectsExpectingFailure {
            setup(it) {
                val functionResource = cfDocuments["dev"]!!.updateTemplate.resources.getFunction("handlers.UsesBasicFunctionHandler", "func2")!!

                usesBasicServerlessFunctionClientProcessor.handleUseResources(it.elementUtils.getTypeElement("handlers.UsesBasicFunctionHandler").enclosedElements[2], functionResource)

                verify { messager.printMessage(Diagnostic.Kind.ERROR, any(), any()) }
            }
        }
    }
}
