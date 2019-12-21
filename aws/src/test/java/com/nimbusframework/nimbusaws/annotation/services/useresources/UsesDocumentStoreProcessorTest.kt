package com.nimbusframework.nimbusaws.annotation.services.useresources

import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.annotation.services.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.annotation.services.functions.DocumentStoreFunctionResourceCreator
import com.nimbusframework.nimbusaws.annotation.services.functions.HttpFunctionResourceCreator
import com.nimbusframework.nimbusaws.annotation.services.resources.DocumentStoreResourceCreator
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.processing.MethodInformation
import com.nimbusframework.nimbusaws.cloudformation.resource.function.FunctionResource
import com.nimbusframework.nimbuscore.persisted.NimbusState
import io.kotlintest.specs.AnnotationSpec
import io.kotlintest.shouldBe
import io.mockk.mockk
import models.Document
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.util.Elements

class UsesDocumentStoreProcessorTest : AnnotationSpec() {

    private lateinit var usesDocumentStoreProcessor: UsesDocumentStoreProcessor
    private lateinit var roundEnvironment: RoundEnvironment
    private lateinit var cfDocuments: MutableMap<String, CloudFormationFiles>
    private lateinit var nimbusState: NimbusState
    private lateinit var elements: Elements
    private lateinit var functionResource: FunctionResource

    @BeforeEach
    fun setup() {
        nimbusState = NimbusState()
        cfDocuments = mutableMapOf()
        roundEnvironment = mockk()

        val compileState = CompileStateService("models/Document.java", "handlers/UsesDocumentStoreHandler.java")
        elements = compileState.elements
        DocumentStoreResourceCreator(roundEnvironment, cfDocuments, nimbusState).handleAgnosticType(elements.getTypeElement("models.Document"));

        HttpFunctionResourceCreator(cfDocuments, nimbusState, compileState.processingEnvironment).handleElement(elements.getTypeElement("handlers.UsesDocumentStoreHandler").enclosedElements[1], FunctionEnvironmentService(cfDocuments, nimbusState), mutableListOf())

        functionResource = cfDocuments["dev"]!!.updateTemplate.resources.get("UsesDocumentStoreHandlerfuncFunction") as FunctionResource

        usesDocumentStoreProcessor = UsesDocumentStoreProcessor(cfDocuments, compileState.processingEnvironment, nimbusState)
    }

    @Test
    fun correctlyCompiles() {
        usesDocumentStoreProcessor.handleUseResources(elements.getTypeElement("handlers.UsesDocumentStoreHandler").enclosedElements[1], functionResource)
    }



}