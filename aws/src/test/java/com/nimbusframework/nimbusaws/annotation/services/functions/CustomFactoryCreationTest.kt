package com.nimbusframework.nimbusaws.annotation.services.functions

import com.google.testing.compile.Compilation
import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.annotation.processor.FunctionInformation
import com.nimbusframework.nimbusaws.annotation.services.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbuscore.persisted.NimbusState
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.mockk
import io.mockk.verify
import javax.annotation.processing.Messager
import javax.annotation.processing.RoundEnvironment
import javax.tools.Diagnostic

class CustomFactoryCreationTest: AnnotationSpec() {

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
        functionEnvironmentService = FunctionEnvironmentService(cfDocuments, nimbusState)
    }

    @Test
    fun correctlyProcessesCustomFactory() {
        compileState = CompileStateService("handlers/CustomFactoryFactory.java", "handlers/CustomFactoryHandler.java")
        compileState.compileObjects { processingEnv ->
            basicFunctionResourceCreator = BasicFunctionResourceCreator(cfDocuments, nimbusState, processingEnv, mockk(relaxed = true))
            val results: MutableList<FunctionInformation> = mutableListOf()
            val classElem = processingEnv.elementUtils.getTypeElement("handlers.CustomFactoryHandler")
            val funcElem = classElem.enclosedElements[2]
            basicFunctionResourceCreator.handleElement(funcElem, functionEnvironmentService, results)
            cfDocuments["dev"] shouldNotBe null

            val resources = cfDocuments["dev"]!!.updateTemplate.resources
            resources.size() shouldBe 4

            results.size shouldBe 1
        }
        compileState.status shouldBe Compilation.Status.SUCCESS
    }

    @Test
    fun correctlyProcessesCustomFactoryWrongInterface() {
        compileState = CompileStateService("handlers/CustomFactoryHandlerWrongFactory.java", "handlers/CustomFactoryWrongFactory.java")
        val messager = mockk<Messager>(relaxed = true)
        compileState.compileObjects { processingEnv ->
            basicFunctionResourceCreator = BasicFunctionResourceCreator(cfDocuments, nimbusState, processingEnv, messager)
            val results: MutableList<FunctionInformation> = mutableListOf()
            val classElem = processingEnv.elementUtils.getTypeElement("handlers.CustomFactoryHandlerWrongFactory")
            val funcElem = classElem.enclosedElements[2]
            basicFunctionResourceCreator.handleElement(funcElem, functionEnvironmentService, results)

            verify { messager.printMessage(Diagnostic.Kind.ERROR, "Custom factory handlers.CustomFactoryWrongFactory does not implement CustomFactory<CustomFactoryHandlerWrongFactory>", any()) }
        }
        compileState.status shouldBe Compilation.Status.FAILURE
    }
}