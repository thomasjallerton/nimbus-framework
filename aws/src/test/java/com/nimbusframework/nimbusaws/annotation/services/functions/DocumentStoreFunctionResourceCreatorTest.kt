package com.nimbusframework.nimbusaws.annotation.services.functions

import com.google.testing.compile.Compilation
import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.annotation.processor.FunctionInformation
import com.nimbusframework.nimbusaws.annotation.services.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.annotation.services.ResourceFinder
import com.nimbusframework.nimbusaws.annotation.services.resources.DocumentStoreResourceCreator
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.resource.dynamo.DynamoResource
import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbuscore.wrappers.DynamoConfiguration
import io.kotlintest.specs.AnnotationSpec
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.util.Elements

class DocumentStoreFunctionResourceCreatorTest : AnnotationSpec() {

    private lateinit var documentStoreFunctionResourceCreator: DocumentStoreFunctionResourceCreator
    private lateinit var roundEnvironment: RoundEnvironment
    private lateinit var cfDocuments: MutableMap<String, CloudFormationFiles>
    private lateinit var nimbusState: NimbusState
    private lateinit var elements: Elements
    private lateinit var functionEnvironmentService: FunctionEnvironmentService
    private lateinit var resourceFinder: ResourceFinder

    @BeforeEach
    fun setup() {
        nimbusState = NimbusState()
        cfDocuments = mutableMapOf()
        roundEnvironment = mockk()
        resourceFinder = mockk()

        val compileState = CompileStateService("handlers/DocumentStoreHandlers.java")
        compileState.status shouldBe Compilation.Status.SUCCESS

        elements = compileState.elements
        functionEnvironmentService = FunctionEnvironmentService(cfDocuments, nimbusState)
        documentStoreFunctionResourceCreator = DocumentStoreFunctionResourceCreator(cfDocuments, nimbusState, compileState.processingEnvironment, resourceFinder)
    }

    @Test
    fun correctlyProcessesDocumentStoreFunctionAnnotation() {
        every { resourceFinder.getDocumentStoreResource(any(), any(), any()) } returns DynamoResource(DynamoConfiguration("table"), nimbusState, "dev")
        val results: MutableList<FunctionInformation> = mutableListOf()
        val classElem = elements.getTypeElement("handlers.DocumentStoreHandlers")
        val funcElem = classElem.enclosedElements[1]
        documentStoreFunctionResourceCreator.handleElement(funcElem, functionEnvironmentService, results)
        cfDocuments["dev"] shouldNotBe null

        val resources = cfDocuments["dev"]!!.updateTemplate.resources
        resources.size() shouldBe 5

        results.size shouldBe 1
    }

}