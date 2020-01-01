package com.nimbusframework.nimbusaws.annotation.services.useresources

import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.annotation.services.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.annotation.services.functions.HttpFunctionResourceCreator
import com.nimbusframework.nimbusaws.annotation.services.resources.DocumentStoreResourceCreator
import com.nimbusframework.nimbusaws.annotation.services.resources.KeyValueStoreResourceCreator
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.resource.IamRoleResource
import com.nimbusframework.nimbusaws.cloudformation.resource.function.FunctionResource
import com.nimbusframework.nimbuscore.persisted.ClientType
import com.nimbusframework.nimbuscore.persisted.NimbusState
import io.kotlintest.shouldBe
import io.kotlintest.specs.AnnotationSpec
import io.mockk.mockk
import io.mockk.verify
import javax.annotation.processing.Messager
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.util.Elements
import javax.tools.Diagnostic

class UsesKeyValueStoreProcessorTest: AnnotationSpec() {

    private lateinit var usesKeyValueStoreProcessor: UsesKeyValueStoreProcessor
    private lateinit var roundEnvironment: RoundEnvironment
    private lateinit var cfDocuments: MutableMap<String, CloudFormationFiles>
    private lateinit var nimbusState: NimbusState
    private lateinit var elements: Elements
    private lateinit var iamRoleResource: IamRoleResource
    private lateinit var messager: Messager

    @BeforeEach
    fun setup() {
        nimbusState = NimbusState()
        cfDocuments = mutableMapOf()
        roundEnvironment = mockk()
        messager = mockk(relaxed = true)

        val compileState = CompileStateService("models/KeyValue.java", "handlers/UsesKeyValueStoreHandler.java")
        elements = compileState.elements

        KeyValueStoreResourceCreator(roundEnvironment, cfDocuments, nimbusState, compileState.processingEnvironment).handleAgnosticType(elements.getTypeElement("models.KeyValue"))

        HttpFunctionResourceCreator(cfDocuments, nimbusState, compileState.processingEnvironment).handleElement(elements.getTypeElement("handlers.UsesKeyValueStoreHandler").enclosedElements[1], FunctionEnvironmentService(cfDocuments, nimbusState), mutableListOf())
        HttpFunctionResourceCreator(cfDocuments, nimbusState, compileState.processingEnvironment).handleElement(elements.getTypeElement("handlers.UsesKeyValueStoreHandler").enclosedElements[2], FunctionEnvironmentService(cfDocuments, nimbusState), mutableListOf())

        iamRoleResource = cfDocuments["dev"]!!.updateTemplate.resources.get("IamRolealueStoreHandlerfunc") as IamRoleResource
        usesKeyValueStoreProcessor = UsesKeyValueStoreProcessor(cfDocuments, compileState.processingEnvironment, nimbusState, messager)
    }

    @Test
    fun correctlySetsPermissions() {
        val functionResource = cfDocuments["dev"]!!.updateTemplate.resources.get("UsesKeyValueStoreHandlerfuncFunction") as FunctionResource
        usesKeyValueStoreProcessor.handleUseResources(elements.getTypeElement("handlers.UsesKeyValueStoreHandler").enclosedElements[1], functionResource)
        val dynamoResource = cfDocuments["dev"]!!.updateTemplate.resources.get("KeyValuedev")!!

        functionResource.usesClient(ClientType.KeyValueStore) shouldBe true
        iamRoleResource.allows("dynamodb:*", dynamoResource) shouldBe true
    }

    @Test
    fun reportsErrorIfCannotFindDocumentStore() {
        val functionResource = cfDocuments["dev"]!!.updateTemplate.resources.get("UsesKeyValueStoreHandlerfunc2Function") as FunctionResource
        usesKeyValueStoreProcessor.handleUseResources(elements.getTypeElement("handlers.UsesKeyValueStoreHandler").enclosedElements[2], functionResource)

        verify { messager.printMessage(Diagnostic.Kind.ERROR, any(), any()) }
    }

}