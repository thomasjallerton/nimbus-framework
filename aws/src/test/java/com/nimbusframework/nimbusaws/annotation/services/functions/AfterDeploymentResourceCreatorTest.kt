package com.nimbusframework.nimbusaws.annotation.services.functions

import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.annotation.processor.FunctionInformation
import com.nimbusframework.nimbusaws.annotation.services.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbuscore.persisted.NimbusState
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.mockk

class AfterDeploymentResourceCreatorTest : AnnotationSpec() {

    private lateinit var afterDeploymentFunctionResourceCreator: AfterDeploymentResourceCreator
    private lateinit var cfDocuments: MutableMap<String, CloudFormationFiles>
    private lateinit var nimbusState: NimbusState
    private lateinit var compileState: CompileStateService
    private lateinit var functionEnvironmentService: FunctionEnvironmentService

    @BeforeEach
    fun setup() {
        nimbusState = NimbusState()
        cfDocuments = mutableMapOf()
        compileState = CompileStateService("handlers/AfterDeploymentHandlers.java")
        functionEnvironmentService = FunctionEnvironmentService(cfDocuments, nimbusState)
    }

    @Test
    fun correctlyProcessesAfterDeploymentFunctionAnnotation() {
        compileState.compileObjects { processingEnv ->
            afterDeploymentFunctionResourceCreator = AfterDeploymentResourceCreator(cfDocuments, nimbusState, processingEnv, mockk(relaxed = true))

            val results: MutableList<FunctionInformation> = mutableListOf()
            val classElem = processingEnv.elementUtils.getTypeElement("handlers.AfterDeploymentHandlers")
            val funcElem = classElem.enclosedElements[1]
            afterDeploymentFunctionResourceCreator.handleElement(funcElem, functionEnvironmentService, results)
            cfDocuments["dev"] shouldNotBe null

            val resources = cfDocuments["dev"]!!.updateTemplate.resources
            resources.size() shouldBe 4

            results.size shouldBe 1
        }
    }

}