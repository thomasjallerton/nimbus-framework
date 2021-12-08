package com.nimbusframework.nimbusaws.annotation.services.functions

import com.google.testing.compile.Compilation
import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.annotation.processor.FunctionInformation
import com.nimbusframework.nimbusaws.annotation.processor.ProcessingData
import com.nimbusframework.nimbusaws.annotation.services.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.annotation.services.dependencies.ClassForReflectionService
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbuscore.persisted.NimbusState
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import javax.annotation.processing.Messager
import javax.annotation.processing.RoundEnvironment
import javax.tools.Diagnostic

class BasicFunctionResourceCreatorTest : AnnotationSpec() {

    private lateinit var basicFunctionResourceCreator: BasicFunctionResourceCreator
    private lateinit var roundEnvironment: RoundEnvironment
    private lateinit var cfDocuments: MutableMap<String, CloudFormationFiles>
    private lateinit var processingData: ProcessingData
    private lateinit var functionEnvironmentService: FunctionEnvironmentService
    private lateinit var compileState: CompileStateService

    @BeforeEach
    fun setup() {
        processingData = ProcessingData(NimbusState(customRuntime = true))
        cfDocuments = mutableMapOf()
        roundEnvironment = mockk()
        compileState = CompileStateService("handlers/BasicHandlers.java")
        functionEnvironmentService = FunctionEnvironmentService(cfDocuments, processingData.nimbusState)
    }

    @Test
    fun correctlyProcessesBasicFunctionAnnotation() {
        compileState.compileObjects { processingEnv ->
            val classForReflectionService = ClassForReflectionService(processingData, processingEnv.typeUtils)
            basicFunctionResourceCreator = BasicFunctionResourceCreator(cfDocuments, processingData, classForReflectionService, processingEnv, setOf(), mockk(relaxed = true))
            val classElem = processingEnv.elementUtils.getTypeElement("handlers.BasicHandlers")
            val funcElem = classElem.enclosedElements[2]
            val results = basicFunctionResourceCreator.handleElement(funcElem, functionEnvironmentService)
            cfDocuments["dev"] shouldNotBe null

            val resources = cfDocuments["dev"]!!.updateTemplate.resources
            resources.size() shouldBe 4

            results.size shouldBe 1
            processingData.classesForReflection shouldContain "models.Person"
        }
        compileState.status shouldBe Compilation.Status.SUCCESS
    }

    @Test
    fun correctlyProcessesBasicCustomFactoryNoEmptyConstructor() {
        compileState = CompileStateService("handlers/CustomFactoryHandlerNoEmptyConstructor.java", "handlers/CustomFactoryFactoryNoEmptyConstructor.java")
        val messager = mockk<Messager>(relaxed = true)
        compileState.compileObjects { processingEnv ->
            val classForReflectionService = ClassForReflectionService(processingData, processingEnv.typeUtils)
            basicFunctionResourceCreator = BasicFunctionResourceCreator(cfDocuments, processingData, classForReflectionService, processingEnv, setOf(), messager)
            every { messager.printMessage(Diagnostic.Kind.ERROR, any(), any()) } answers { processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "") }
            val classElem = processingEnv.elementUtils.getTypeElement("handlers.CustomFactoryHandlerNoEmptyConstructor")
            val funcElem = classElem.enclosedElements[2]
            basicFunctionResourceCreator.handleElement(funcElem, functionEnvironmentService)

            verify { messager.printMessage(Diagnostic.Kind.ERROR, "handlers.CustomFactoryHandlerNoEmptyConstructor must have a non-parameterized constructor", any()) }
        }
        compileState.status shouldBe Compilation.Status.FAILURE
    }

    @Test
    fun correctlyProcessesBasicFunctionCronAnnotation() {
        compileState.compileObjects { processingEnv ->
            val classForReflectionService = ClassForReflectionService(processingData, processingEnv.typeUtils)
            basicFunctionResourceCreator = BasicFunctionResourceCreator(cfDocuments, processingData, classForReflectionService, processingEnv, setOf(), mockk(relaxed = true))
            val classElem = processingEnv.elementUtils.getTypeElement("handlers.BasicHandlers")
            val funcElem = classElem.enclosedElements[1]
            val results = basicFunctionResourceCreator.handleElement(funcElem, functionEnvironmentService)
            cfDocuments["dev"] shouldNotBe null

            val resources = cfDocuments["dev"]!!.updateTemplate.resources
            resources.size() shouldBe 6

            results.size shouldBe 1
        }
        compileState.status shouldBe Compilation.Status.SUCCESS
    }

    @Test
    fun correctlyProcessesBasicFunctionJustReturn() {
        compileState.compileObjects { processingEnv ->
            val classForReflectionService = ClassForReflectionService(processingData, processingEnv.typeUtils)
            basicFunctionResourceCreator = BasicFunctionResourceCreator(cfDocuments, processingData, classForReflectionService, processingEnv, setOf(), mockk(relaxed = true))
            val classElem = processingEnv.elementUtils.getTypeElement("handlers.BasicHandlers")
            val funcElem = classElem.enclosedElements[4]
            val results = basicFunctionResourceCreator.handleElement(funcElem, functionEnvironmentService)
            cfDocuments["dev"] shouldNotBe null

            val resources = cfDocuments["dev"]!!.updateTemplate.resources
            resources.size() shouldBe 4

            results.size shouldBe 1
            processingData.classesForReflection shouldContain "models.Person"
        }
        compileState.status shouldBe Compilation.Status.SUCCESS
    }

    @Test
    fun correctlyProcessesBasicFunctionJustInput() {
        compileState.compileObjects { processingEnv ->
            val classForReflectionService = ClassForReflectionService(processingData, processingEnv.typeUtils)
            basicFunctionResourceCreator = BasicFunctionResourceCreator(cfDocuments, processingData, classForReflectionService, processingEnv, setOf(), mockk(relaxed = true))
            val classElem = processingEnv.elementUtils.getTypeElement("handlers.BasicHandlers")
            val funcElem = classElem.enclosedElements[5]
            val results = basicFunctionResourceCreator.handleElement(funcElem, functionEnvironmentService)
            cfDocuments["dev"] shouldNotBe null

            val resources = cfDocuments["dev"]!!.updateTemplate.resources
            resources.size() shouldBe 4

            results.size shouldBe 1
            processingData.classesForReflection shouldContain "models.Person"
        }
        compileState.status shouldBe Compilation.Status.SUCCESS
    }
}
