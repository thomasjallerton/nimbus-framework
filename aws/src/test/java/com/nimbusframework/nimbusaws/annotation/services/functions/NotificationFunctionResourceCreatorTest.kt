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

class NotificationFunctionResourceCreatorTest : AnnotationSpec() {

    private lateinit var notificationStoreFunctionResourceCreator: NotificationFunctionResourceCreator
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
        val compileState = CompileStateService("handlers/NotificationHandlers.java")
        elements = compileState.elements
        functionEnvironmentService = FunctionEnvironmentService(cfDocuments, nimbusState)
        notificationStoreFunctionResourceCreator = NotificationFunctionResourceCreator(cfDocuments, nimbusState, compileState.processingEnvironment)
    }

    @Test
    fun correctlyProcessesNotificationFunctionAnnotation() {
        val results: MutableList<FunctionInformation> = mutableListOf()
        val classElem = elements.getTypeElement("handlers.NotificationHandlers")
        val funcElem = classElem.enclosedElements[1]
        notificationStoreFunctionResourceCreator.handleElement(funcElem, functionEnvironmentService, results)
        cfDocuments["dev"] shouldNotBe null

        val resources = cfDocuments["dev"]!!.updateTemplate.resources
        resources.size() shouldBe 6

        results.size shouldBe 1
    }

}