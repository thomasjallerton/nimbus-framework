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

class QueueFunctionResourceCreatorTest : AnnotationSpec() {

    private lateinit var queueFunctionResourceCreator: QueueFunctionResourceCreator
    private lateinit var roundEnvironment: RoundEnvironment
    private lateinit var cfDocuments: MutableMap<String, CloudFormationFiles>
    private lateinit var nimbusState: NimbusState
    private lateinit var functionEnvironmentService: FunctionEnvironmentService
    private lateinit var compileStateService: CompileStateService

    @BeforeEach
    fun setup() {
        nimbusState = NimbusState()
        cfDocuments = mutableMapOf()
        roundEnvironment = mockk()
        compileStateService = CompileStateService("handlers/QueueHandlers.java")
        functionEnvironmentService = FunctionEnvironmentService(cfDocuments, nimbusState)
    }

    @Test
    fun correctlyProcessesQueueFunctionAnnotation() {
        compileStateService.compileObjects {
            queueFunctionResourceCreator = QueueFunctionResourceCreator(cfDocuments, nimbusState, it)

            val results: MutableList<FunctionInformation> = mutableListOf()
            val classElem = it.elementUtils.getTypeElement("handlers.QueueHandlers")
            val funcElem = classElem.enclosedElements[1]
            queueFunctionResourceCreator.handleElement(funcElem, functionEnvironmentService, results)
            cfDocuments["dev"] shouldNotBe null

            val resources = cfDocuments["dev"]!!.updateTemplate.resources
            resources.size() shouldBe 6

            results.size shouldBe 1
        }
    }

}