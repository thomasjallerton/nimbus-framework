package com.nimbusframework.nimbusaws.annotation.services.functions

import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.annotation.processor.FunctionInformation
import com.nimbusframework.nimbusaws.annotation.services.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.annotation.services.ResourceFinder
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.resource.dynamo.DynamoResource
import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbuscore.wrappers.DynamoConfiguration
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import javax.annotation.processing.RoundEnvironment

class DocumentStoreFunctionResourceCreatorTest : AnnotationSpec() {

    private lateinit var documentStoreFunctionResourceCreator: DocumentStoreFunctionResourceCreator
    private lateinit var roundEnvironment: RoundEnvironment
    private lateinit var cfDocuments: MutableMap<String, CloudFormationFiles>
    private lateinit var nimbusState: NimbusState
    private lateinit var functionEnvironmentService: FunctionEnvironmentService
    private lateinit var resourceFinder: ResourceFinder
    private lateinit var compileState: CompileStateService

    @BeforeEach
    fun setup() {
        nimbusState = NimbusState()
        cfDocuments = mutableMapOf()
        roundEnvironment = mockk()
        resourceFinder = mockk()

        compileState = CompileStateService("handlers/DocumentStoreHandlers.java")

        functionEnvironmentService = FunctionEnvironmentService(cfDocuments, nimbusState)
    }

    @Test
    fun correctlyProcessesDocumentStoreFunctionAnnotation() {
        compileState.compileObjects {processingEnvironment ->
            documentStoreFunctionResourceCreator = DocumentStoreFunctionResourceCreator(cfDocuments, nimbusState, processingEnvironment, mockk(relaxed = true), resourceFinder)
            every { resourceFinder.getDocumentStoreResource(any(), any(), any()) } returns DynamoResource(DynamoConfiguration("table"), nimbusState, "dev")
            val results: MutableList<FunctionInformation> = mutableListOf()
            val classElem = processingEnvironment.elementUtils.getTypeElement("handlers.DocumentStoreHandlers")
            val funcElem = classElem.enclosedElements[1]
            documentStoreFunctionResourceCreator.handleElement(funcElem, functionEnvironmentService, results)
            cfDocuments["dev"] shouldNotBe null

            val resources = cfDocuments["dev"]!!.updateTemplate.resources
            resources.size() shouldBe 5

            results.size shouldBe 1
        }

    }

}