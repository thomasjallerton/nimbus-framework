package com.nimbusframework.nimbusaws.cloudformation.generation.resources.basicfunction

import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.annotation.processor.ProcessingData
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.basicfunction.AfterDeploymentResourceCreator
import com.nimbusframework.nimbusaws.cloudformation.model.CloudFormationFiles
import com.nimbusframework.nimbuscore.persisted.NimbusState
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.mockk

class AfterDeploymentResourceCreatorTest : AnnotationSpec() {

    private lateinit var afterDeploymentFunctionResourceCreator: AfterDeploymentResourceCreator
    private lateinit var cfDocuments: MutableMap<String, CloudFormationFiles>
    private lateinit var processingData: ProcessingData
    private lateinit var compileState: CompileStateService
    private lateinit var functionEnvironmentService: FunctionEnvironmentService

    @BeforeEach
    fun setup() {
        processingData = ProcessingData(NimbusState())
        cfDocuments = mutableMapOf()
        compileState = CompileStateService("handlers/AfterDeploymentHandlers.java")
        functionEnvironmentService = FunctionEnvironmentService(cfDocuments, processingData)
    }

    @Test
    fun correctlyProcessesAfterDeploymentFunctionAnnotation() {
        compileState.compileObjects { processingEnv ->
            afterDeploymentFunctionResourceCreator = AfterDeploymentResourceCreator(cfDocuments, processingData, mockk(relaxed = true), processingEnv, setOf(), mockk(relaxed = true))

            val classElem = processingEnv.elementUtils.getTypeElement("handlers.AfterDeploymentHandlers")
            val funcElem = classElem.enclosedElements[1]
            val results = afterDeploymentFunctionResourceCreator.handleElement(funcElem, functionEnvironmentService)
            cfDocuments["dev"] shouldNotBe null

            val resources = cfDocuments["dev"]!!.updateTemplate.resources
            resources.size() shouldBe 2

            results.size shouldBe 1
            results[0].canBeKeptWarm shouldBe false
        }
    }

}
