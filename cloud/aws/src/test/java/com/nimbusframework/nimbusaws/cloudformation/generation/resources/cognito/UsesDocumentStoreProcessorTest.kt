package com.nimbusframework.nimbusaws.cloudformation.generation.resources.cognito

import com.google.testing.compile.Compilation
import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.annotation.processor.ProcessingData
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.apigateway.HttpFunctionResourceCreator
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.cognito.UsesDocumentStoreProcessor
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.dynamo.DocumentStoreResourceCreator
import com.nimbusframework.nimbusaws.cloudformation.model.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.model.resource.IamRoleResource
import com.nimbusframework.nimbusaws.cloudformation.model.resource.function.FunctionResource
import com.nimbusframework.nimbuscore.persisted.NimbusState
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import io.mockk.verify
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.tools.Diagnostic

class UsesDocumentStoreProcessorTest : AnnotationSpec() {

    private lateinit var usesDocumentStoreProcessor: UsesDocumentStoreProcessor
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
        messager = mockk(relaxed = true)

        compileStateService = CompileStateService("models/Document.java", "handlers/UsesDocumentStoreHandler.java")
    }

    private fun setup(processingEnvironment: ProcessingEnvironment, toRun: () -> Unit ) {
        val elements = processingEnvironment.elementUtils
        DocumentStoreResourceCreator(roundEnvironment, cfDocuments, processingData, mockk(relaxed = true)).handleAgnosticType(elements.getTypeElement("models.Document"))

        HttpFunctionResourceCreator(cfDocuments, processingData, mockk(relaxed = true), processingEnvironment, setOf(), mockk(relaxed = true)).handleElement(elements.getTypeElement("handlers.UsesDocumentStoreHandler").enclosedElements[1], FunctionEnvironmentService(cfDocuments, processingData))
        HttpFunctionResourceCreator(cfDocuments, processingData, mockk(relaxed = true), processingEnvironment, setOf(), mockk(relaxed = true)).handleElement(elements.getTypeElement("handlers.UsesDocumentStoreHandler").enclosedElements[2], FunctionEnvironmentService(cfDocuments, processingData))

        usesDocumentStoreProcessor = UsesDocumentStoreProcessor(cfDocuments, processingEnvironment, nimbusState, messager)

        toRun()
    }

    @Test
    fun correctlySetsPermissions() {
        compileStateService.compileObjects {
            setup(it) {
                val functionResource = cfDocuments["dev"]!!.updateTemplate.resources.get("UsesDocumentStoreHandlerfuncFunction") as FunctionResource
                usesDocumentStoreProcessor.handleUseResources(it.elementUtils.getTypeElement("handlers.UsesDocumentStoreHandler").enclosedElements[1], functionResource)
                val dynamoResource = cfDocuments["dev"]!!.updateTemplate.resources.get("Documentdev")!!

                functionResource.iamRoleResource.allows("dynamodb:*", dynamoResource) shouldBe true
            }
        }
        compileStateService.status shouldBe Compilation.Status.SUCCESS
    }

    @Test
    fun reportsErrorIfCannotFindDocumentStore() {
        compileStateService.compileObjectsExpectingFailure {
            setup(it) {
                val functionResource = cfDocuments["dev"]!!.updateTemplate.resources.get("UsesDocumentStoreHandlerfunc2Function") as FunctionResource
                usesDocumentStoreProcessor.handleUseResources(it.elementUtils.getTypeElement("handlers.UsesDocumentStoreHandler").enclosedElements[2], functionResource)

                verify { messager.printMessage(Diagnostic.Kind.ERROR, any(), any()) }
            }
        }
    }

}
