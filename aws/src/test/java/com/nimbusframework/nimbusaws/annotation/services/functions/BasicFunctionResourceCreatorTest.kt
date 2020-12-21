package com.nimbusframework.nimbusaws.annotation.services.functions

import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.annotation.processor.FunctionInformation
import com.nimbusframework.nimbusaws.annotation.services.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.annotation.services.resources.DocumentStoreResourceCreator
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.resource.dynamo.DynamoResource
import com.nimbusframework.nimbuscore.persisted.NimbusState
import io.kotlintest.specs.AnnotationSpec
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.mockk.mockk
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.util.Elements

class BasicFunctionResourceCreatorTest : AnnotationSpec() {

    private lateinit var basicFunctionResourceCreator: BasicFunctionResourceCreator
    private lateinit var roundEnvironment: RoundEnvironment
    private lateinit var cfDocuments: MutableMap<String, CloudFormationFiles>
    private lateinit var nimbusState: NimbusState
    private lateinit var functionEnvironmentService: FunctionEnvironmentService
    private lateinit var compileState: CompileStateService

    @BeforeEach
    fun setup() {
        nimbusState = NimbusState()
        cfDocuments = mutableMapOf()
        roundEnvironment = mockk()
        compileState = CompileStateService("handlers/BasicHandlers.java")
        functionEnvironmentService = FunctionEnvironmentService(cfDocuments, nimbusState)
    }

    @Test
    fun correctlyProcessesBasicFunctionAnnotation() {
        compileState.compileObjects { processingEnv ->
            basicFunctionResourceCreator = BasicFunctionResourceCreator(cfDocuments, nimbusState, processingEnv)
            val results: MutableList<FunctionInformation> = mutableListOf()
            val classElem = processingEnv.elementUtils.getTypeElement("handlers.BasicHandlers")
            val funcElem = classElem.enclosedElements[2]
            basicFunctionResourceCreator.handleElement(funcElem, functionEnvironmentService, results)
            cfDocuments["dev"] shouldNotBe null

            val resources = cfDocuments["dev"]!!.updateTemplate.resources
            resources.size() shouldBe 4

            results.size shouldBe 1
        }
    }

    @Test
    fun correctlyProcessesBasicFunctionCronAnnotation() {
        compileState.compileObjects { processingEnv ->
            basicFunctionResourceCreator = BasicFunctionResourceCreator(cfDocuments, nimbusState, processingEnv)
            val results: MutableList<FunctionInformation> = mutableListOf()
            val classElem = processingEnv.elementUtils.getTypeElement("handlers.BasicHandlers")
            val funcElem = classElem.enclosedElements[1]
            basicFunctionResourceCreator.handleElement(funcElem, functionEnvironmentService, results)
            cfDocuments["dev"] shouldNotBe null

            val resources = cfDocuments["dev"]!!.updateTemplate.resources
            resources.size() shouldBe 6

            results.size shouldBe 1
        }
    }

}