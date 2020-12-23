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
import javax.annotation.processing.RoundEnvironment

class HttpFunctionResourceCreatorTest : AnnotationSpec() {

    private lateinit var httpFunctionResourceCreator: HttpFunctionResourceCreator
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
        compileStateService = CompileStateService("handlers/HttpHandlers.java")
        functionEnvironmentService = FunctionEnvironmentService(cfDocuments, nimbusState)
    }

    @Test
    fun correctlyProcessesHttpStoreFunctionAnnotation() {
        compileStateService.compileObjects {
            httpFunctionResourceCreator = HttpFunctionResourceCreator(cfDocuments, nimbusState, it, mockk(relaxed = true))

            val results: MutableList<FunctionInformation> = mutableListOf()
            val classElem = it.elementUtils.getTypeElement("handlers.HttpHandlers")
            val funcElem = classElem.enclosedElements[1]
            httpFunctionResourceCreator.handleElement(funcElem, functionEnvironmentService, results)
            cfDocuments["dev"] shouldNotBe null

            val resources = cfDocuments["dev"]!!.updateTemplate.resources
            resources.size() shouldBe 9

            results.size shouldBe 1
        }
    }

    @Test
    fun correctlyProcessesHttpStoreFunctionAnnotationWithLongerPath() {
        compileStateService.compileObjects {
            httpFunctionResourceCreator = HttpFunctionResourceCreator(cfDocuments, nimbusState, it, mockk(relaxed = true))
            val results: MutableList<FunctionInformation> = mutableListOf()
            val classElem = it.elementUtils.getTypeElement("handlers.HttpHandlers")
            val funcElem = classElem.enclosedElements[2]
            httpFunctionResourceCreator.handleElement(funcElem, functionEnvironmentService, results)
            cfDocuments["dev"] shouldNotBe null

            val resources = cfDocuments["dev"]!!.updateTemplate.resources
            resources.size() shouldBe 10

            results.size shouldBe 1
        }
    }

}