package com.nimbusframework.nimbusaws.annotation.services.functions

import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.annotation.processor.FunctionInformation
import com.nimbusframework.nimbusaws.annotation.services.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbuscore.persisted.NimbusState
import io.kotlintest.specs.AnnotationSpec
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.mockk.mockk
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.util.Elements

class WebSocketFunctionResourceCreatorTest : AnnotationSpec() {

    private lateinit var webSocketFunctionResourceCreator: WebSocketFunctionResourceCreator
    private lateinit var roundEnvironment: RoundEnvironment
    private lateinit var cfDocuments: MutableMap<String, CloudFormationFiles>
    private lateinit var nimbusState: NimbusState
    private lateinit var elements: Elements
    private lateinit var functionEnvironmentService: FunctionEnvironmentService

    @BeforeEach
    fun setup() {
        nimbusState = NimbusState()
        cfDocuments = mutableMapOf()
        roundEnvironment = mockk()
        val compileState = CompileStateService("handlers/WebSocketHandlers.java")
        elements = compileState.elements
        functionEnvironmentService = FunctionEnvironmentService(cfDocuments, nimbusState)
        webSocketFunctionResourceCreator = WebSocketFunctionResourceCreator(cfDocuments, nimbusState, compileState.processingEnvironment)
    }

    @Test
    fun correctlyProcessesWebSocketFunctionAnnotation() {
        val results: MutableList<FunctionInformation> = mutableListOf()
        val classElem = elements.getTypeElement("handlers.WebSocketHandlers")
        val funcElem = classElem.enclosedElements[1]
        webSocketFunctionResourceCreator.handleElement(funcElem, functionEnvironmentService, results)
        cfDocuments["dev"] shouldNotBe null

        val resources = cfDocuments["dev"]!!.updateTemplate.resources
        resources.size() shouldBe 10

        results.size shouldBe 1
    }

}