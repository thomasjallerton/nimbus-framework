package com.nimbusframework.nimbusaws.annotation.services.useresources

import com.google.testing.compile.Compilation
import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.annotation.services.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.annotation.services.functions.HttpFunctionResourceCreator
import com.nimbusframework.nimbusaws.annotation.services.resources.KeyValueStoreResourceCreator
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

class UsesKeyValueStoreProcessorTest: AnnotationSpec() {

    private lateinit var usesKeyValueStoreProcessor: UsesKeyValueStoreProcessor
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

        compileStateService = CompileStateService("models/KeyValue.java", "handlers/UsesKeyValueStoreHandler.java")

    }

    private fun setup(processingEnvironment: ProcessingEnvironment, toRun: () -> Unit ) {
        val elements = processingEnvironment.elementUtils
        KeyValueStoreResourceCreator(roundEnvironment, cfDocuments, nimbusState, processingEnvironment).handleAgnosticType(elements.getTypeElement("models.KeyValue"))

        HttpFunctionResourceCreator(cfDocuments, nimbusState, processingEnvironment, mockk(relaxed = true)).handleElement(elements.getTypeElement("handlers.UsesKeyValueStoreHandler").enclosedElements[1], FunctionEnvironmentService(cfDocuments, nimbusState), mutableListOf())
        HttpFunctionResourceCreator(cfDocuments, nimbusState, processingEnvironment, mockk(relaxed = true)).handleElement(elements.getTypeElement("handlers.UsesKeyValueStoreHandler").enclosedElements[2], FunctionEnvironmentService(cfDocuments, nimbusState), mutableListOf())

        iamRoleResource = cfDocuments["dev"]!!.updateTemplate.resources.get("IamRolealueStoreHandlerfunc") as IamRoleResource
        usesKeyValueStoreProcessor = UsesKeyValueStoreProcessor(cfDocuments, processingEnvironment, nimbusState, messager)

        toRun()
    }

    @Test
    fun correctlySetsPermissions() {
        compileStateService.compileObjects {
            setup(it) {
                val functionResource = cfDocuments["dev"]!!.updateTemplate.resources.get("UsesKeyValueStoreHandlerfuncFunction") as FunctionResource
                usesKeyValueStoreProcessor.handleUseResources(it.elementUtils.getTypeElement("handlers.UsesKeyValueStoreHandler").enclosedElements[1], functionResource)
                val dynamoResource = cfDocuments["dev"]!!.updateTemplate.resources.get("KeyValuedev")!!

                functionResource.usesClient(ClientType.KeyValueStore) shouldBe true
                iamRoleResource.allows("dynamodb:*", dynamoResource) shouldBe true
            }
        }
        compileStateService.status shouldBe Compilation.Status.SUCCESS

    }

    @Test
    fun reportsErrorIfCannotFindDocumentStore() {
        compileStateService.compileObjects {
            setup(it) {
                val functionResource = cfDocuments["dev"]!!.updateTemplate.resources.get("UsesKeyValueStoreHandlerfunc2Function") as FunctionResource
                usesKeyValueStoreProcessor.handleUseResources(it.elementUtils.getTypeElement("handlers.UsesKeyValueStoreHandler").enclosedElements[2], functionResource)

                verify { messager.printMessage(Diagnostic.Kind.ERROR, any(), any()) }
            }
        }
    }

}