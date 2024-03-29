package com.nimbusframework.nimbusaws.cloudformation.generation.resources.apigateway

import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.annotation.processor.ProcessingData
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.ClassForReflectionService
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.apigateway.WebSocketFunctionResourceCreator
import com.nimbusframework.nimbusaws.cloudformation.model.CloudFormationFiles
import com.nimbusframework.nimbuscore.eventabstractions.WebSocketEvent
import com.nimbusframework.nimbuscore.eventabstractions.WebSocketResponse
import com.nimbusframework.nimbuscore.persisted.NimbusState
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.mockk
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment

class WebSocketFunctionResourceCreatorTest : AnnotationSpec() {

    private lateinit var webSocketFunctionResourceCreator: WebSocketFunctionResourceCreator
    private lateinit var roundEnvironment: RoundEnvironment
    private lateinit var cfDocuments: MutableMap<String, CloudFormationFiles>
    private lateinit var processingData: ProcessingData
    private lateinit var functionEnvironmentService: FunctionEnvironmentService
    private lateinit var compileStateService: CompileStateService
    @BeforeEach
    fun setup() {
        processingData = ProcessingData(NimbusState(customRuntime = true))
        cfDocuments = mutableMapOf()
        roundEnvironment = mockk()
        compileStateService = CompileStateService("handlers/WebSocketHandlers.java")
    }

    private fun setup(processingEnvironment: ProcessingEnvironment, toRun: () -> Unit ) {
        functionEnvironmentService = FunctionEnvironmentService(cfDocuments, processingData)
        val classForReflectionService = ClassForReflectionService(processingData, processingEnvironment.typeUtils)
        webSocketFunctionResourceCreator = WebSocketFunctionResourceCreator(cfDocuments, processingData, classForReflectionService, processingEnvironment, setOf(), mockk(relaxed = true))
        toRun()
    }

    @Test
    fun correctlyProcessesWebSocketFunctionAnnotation() {
        compileStateService.compileObjects {
            setup(it) {
                val classElem = it.elementUtils.getTypeElement("handlers.WebSocketHandlers")
                val funcElem = classElem.enclosedElements[1]
                val results = webSocketFunctionResourceCreator.handleElement(funcElem, functionEnvironmentService)
                cfDocuments["dev"] shouldNotBe null

                val resources = cfDocuments["dev"]!!.updateTemplate.resources
                resources.size() shouldBe 8

                results.size shouldBe 1
                processingData.classesForReflection shouldContain WebSocketEvent::class.qualifiedName
                processingData.classesForReflection shouldContain WebSocketResponse::class.qualifiedName
            }
        }
    }

}
