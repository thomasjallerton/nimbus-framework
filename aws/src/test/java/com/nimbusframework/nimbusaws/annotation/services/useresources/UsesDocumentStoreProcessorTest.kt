package com.nimbusframework.nimbusaws.annotation.services.useresources

import com.google.testing.compile.Compilation
import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.annotation.services.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.annotation.services.functions.HttpFunctionResourceCreator
import com.nimbusframework.nimbusaws.annotation.services.resources.DocumentStoreResourceCreator
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.resource.IamRoleResource
import com.nimbusframework.nimbusaws.cloudformation.resource.function.FunctionResource
import com.nimbusframework.nimbuscore.persisted.ClientType
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
    private lateinit var iamRoleResource: IamRoleResource
    private lateinit var messager: Messager
    private lateinit var compileStateService: CompileStateService

    @BeforeEach
    fun setup() {
        nimbusState = NimbusState()
        cfDocuments = mutableMapOf()
        roundEnvironment = mockk()
        messager = mockk(relaxed = true)

        compileStateService = CompileStateService("models/Document.java", "handlers/UsesDocumentStoreHandler.java")
    }

    private fun setup(processingEnvironment: ProcessingEnvironment, toRun: () -> Unit ) {
        val elements = processingEnvironment.elementUtils
        DocumentStoreResourceCreator(roundEnvironment, cfDocuments, nimbusState).handleAgnosticType(elements.getTypeElement("models.Document"))

        HttpFunctionResourceCreator(cfDocuments, nimbusState, processingEnvironment, mockk(relaxed = true)).handleElement(elements.getTypeElement("handlers.UsesDocumentStoreHandler").enclosedElements[1], FunctionEnvironmentService(cfDocuments, nimbusState))
        HttpFunctionResourceCreator(cfDocuments, nimbusState, processingEnvironment, mockk(relaxed = true)).handleElement(elements.getTypeElement("handlers.UsesDocumentStoreHandler").enclosedElements[2], FunctionEnvironmentService(cfDocuments, nimbusState))

        iamRoleResource = cfDocuments["dev"]!!.updateTemplate.resources.get("IamRolementStoreHandlerfunc") as IamRoleResource
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

                functionResource.usesClient(ClientType.DocumentStore) shouldBe true

                iamRoleResource.allows("dynamodb:*", dynamoResource) shouldBe true
            }
        }
        compileStateService.status shouldBe Compilation.Status.SUCCESS
    }

    @Test
    fun reportsErrorIfCannotFindDocumentStore() {
        compileStateService.compileObjects {
            setup(it) {
                val functionResource = cfDocuments["dev"]!!.updateTemplate.resources.get("UsesDocumentStoreHandlerfunc2Function") as FunctionResource
                usesDocumentStoreProcessor.handleUseResources(it.elementUtils.getTypeElement("handlers.UsesDocumentStoreHandler").enclosedElements[2], functionResource)

                verify { messager.printMessage(Diagnostic.Kind.ERROR, any(), any()) }
            }
        }
    }

}