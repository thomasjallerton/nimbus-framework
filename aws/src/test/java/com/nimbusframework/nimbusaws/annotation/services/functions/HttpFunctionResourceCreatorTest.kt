package com.nimbusframework.nimbusaws.annotation.services.functions

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
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
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.mockk
import javax.annotation.processing.RoundEnvironment

class HttpFunctionResourceCreatorTest : AnnotationSpec() {

    private lateinit var httpFunctionResourceCreator: HttpFunctionResourceCreator
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
        compileStateService = CompileStateService("handlers/HttpHandlers.java")
        functionEnvironmentService = FunctionEnvironmentService(cfDocuments, processingData.nimbusState)
    }

    @Test
    fun correctlyProcessesHttpStoreFunctionAnnotation() {
        compileStateService.compileObjects {
            val classForReflectionService = ClassForReflectionService(processingData, it.typeUtils)
            httpFunctionResourceCreator = HttpFunctionResourceCreator(cfDocuments, processingData, classForReflectionService, it, setOf(), mockk(relaxed = true))

            val classElem = it.elementUtils.getTypeElement("handlers.HttpHandlers")
            val funcElem = classElem.enclosedElements[1]
            val results = httpFunctionResourceCreator.handleElement(funcElem, functionEnvironmentService)
            cfDocuments["dev"] shouldNotBe null

            val resources = cfDocuments["dev"]!!.updateTemplate.resources
            resources.size() shouldBe 9

            results.size shouldBe 1
            processingData.classesForReflection shouldContain "com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent"
            processingData.classesForReflection shouldContain "com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent"
            processingData.classesForReflection shouldContain "com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent\$ProxyRequestContext"
            processingData.classesForReflection shouldContain "com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent\$RequestIdentity"
            processingData.classesForReflection shouldNotContain "models.Person"
        }
    }

    @Test
    fun correctlyProcessesHttpStoreFunctionAnnotationWithLongerPath() {
        compileStateService.compileObjects {
            val classForReflectionService = ClassForReflectionService(processingData, it.typeUtils)
            httpFunctionResourceCreator = HttpFunctionResourceCreator(cfDocuments, processingData, classForReflectionService, it, setOf(), mockk(relaxed = true))
            val classElem = it.elementUtils.getTypeElement("handlers.HttpHandlers")
            val funcElem = classElem.enclosedElements[2]
            val results = httpFunctionResourceCreator.handleElement(funcElem, functionEnvironmentService)
            cfDocuments["dev"] shouldNotBe null

            val resources = cfDocuments["dev"]!!.updateTemplate.resources
            resources.size() shouldBe 10

            results.size shouldBe 1
            processingData.classesForReflection shouldContain APIGatewayProxyRequestEvent::class.qualifiedName
            processingData.classesForReflection shouldContain APIGatewayProxyResponseEvent::class.qualifiedName
            processingData.classesForReflection shouldContain "models.Person"
        }
    }

    @Test
    fun correctlyProcessesHttpStoreFunctionAnnotationComplexReturn() {
        compileStateService.compileObjects {
            val classForReflectionService = ClassForReflectionService(processingData, it.typeUtils)
            httpFunctionResourceCreator = HttpFunctionResourceCreator(cfDocuments, processingData, classForReflectionService, it, setOf(), mockk(relaxed = true))

            val classElem = it.elementUtils.getTypeElement("handlers.HttpHandlers")
            val funcElem = classElem.enclosedElements[4]
            val results = httpFunctionResourceCreator.handleElement(funcElem, functionEnvironmentService)
            cfDocuments["dev"] shouldNotBe null

            val resources = cfDocuments["dev"]!!.updateTemplate.resources
            resources.size() shouldBe 11

            results.size shouldBe 1
            processingData.classesForReflection shouldContain APIGatewayProxyRequestEvent::class.qualifiedName
            processingData.classesForReflection shouldContain APIGatewayProxyResponseEvent::class.qualifiedName
            processingData.classesForReflection shouldContain "models.Person"
        }
    }

    @Test
    fun correctlyProcessesHttpStoreFunctionAnnotationGenericReturn() {
        compileStateService.compileObjects {
            val classForReflectionService = ClassForReflectionService(processingData, it.typeUtils)
            httpFunctionResourceCreator = HttpFunctionResourceCreator(cfDocuments, processingData, classForReflectionService, it, setOf(), mockk(relaxed = true))

            val classElem = it.elementUtils.getTypeElement("handlers.HttpHandlers")
            val funcElem = classElem.enclosedElements[5]
            val results = httpFunctionResourceCreator.handleElement(funcElem, functionEnvironmentService)
            cfDocuments["dev"] shouldNotBe null

            val resources = cfDocuments["dev"]!!.updateTemplate.resources
            resources.size() shouldBe 9

            results.size shouldBe 1
            processingData.classesForReflection shouldContain APIGatewayProxyRequestEvent::class.qualifiedName
            processingData.classesForReflection shouldContain APIGatewayProxyResponseEvent::class.qualifiedName
            processingData.classesForReflection shouldContain "models.Person"
        }
    }

    @Test
    fun correctlyProcessesHttpStoreFunctionAnnotationGenericInput() {
        compileStateService.compileObjects {
            val classForReflectionService = ClassForReflectionService(processingData, it.typeUtils)
            httpFunctionResourceCreator = HttpFunctionResourceCreator(cfDocuments, processingData, classForReflectionService, it, setOf(), mockk(relaxed = true))

            val classElem = it.elementUtils.getTypeElement("handlers.HttpHandlers")
            val funcElem = classElem.enclosedElements[6]
            val results = httpFunctionResourceCreator.handleElement(funcElem, functionEnvironmentService)
            cfDocuments["dev"] shouldNotBe null

            val resources = cfDocuments["dev"]!!.updateTemplate.resources
            resources.size() shouldBe 9

            results.size shouldBe 1
            processingData.classesForReflection shouldContain APIGatewayProxyRequestEvent::class.qualifiedName
            processingData.classesForReflection shouldContain APIGatewayProxyResponseEvent::class.qualifiedName
            processingData.classesForReflection shouldContain "models.Person"
        }
    }

    @Test
    fun correctlyProcessesHttpStoreFunctionAnnotationNestedInput() {
        compileStateService.compileObjects {
            val classForReflectionService = ClassForReflectionService(processingData, it.typeUtils)
            httpFunctionResourceCreator = HttpFunctionResourceCreator(cfDocuments, processingData, classForReflectionService, it, setOf(), mockk(relaxed = true))

            val classElem = it.elementUtils.getTypeElement("handlers.HttpHandlers")
            val funcElem = classElem.enclosedElements[7]
            val results = httpFunctionResourceCreator.handleElement(funcElem, functionEnvironmentService)
            cfDocuments["dev"] shouldNotBe null

            val resources = cfDocuments["dev"]!!.updateTemplate.resources
            resources.size() shouldBe 9

            results.size shouldBe 1
            processingData.classesForReflection shouldContain APIGatewayProxyRequestEvent::class.qualifiedName
            processingData.classesForReflection shouldContain APIGatewayProxyResponseEvent::class.qualifiedName
            processingData.classesForReflection shouldContain "models.Person"
            processingData.classesForReflection shouldContain "models.NestedPerson"
        }
        compileStateService.status shouldBe Compilation.Status.SUCCESS
    }

    @Test
    fun correctlyProcessesHttpStoreFunctionAnnotationNestedResponse() {
        compileStateService.compileObjects {
            val classForReflectionService = ClassForReflectionService(processingData, it.typeUtils)
            httpFunctionResourceCreator = HttpFunctionResourceCreator(cfDocuments, processingData, classForReflectionService, it, setOf(), mockk(relaxed = true))

            val classElem = it.elementUtils.getTypeElement("handlers.HttpHandlers")
            val funcElem = classElem.enclosedElements[8]
            val results = httpFunctionResourceCreator.handleElement(funcElem, functionEnvironmentService)
            cfDocuments["dev"] shouldNotBe null

            val resources = cfDocuments["dev"]!!.updateTemplate.resources
            resources.size() shouldBe 9

            results.size shouldBe 1
            processingData.classesForReflection shouldContain APIGatewayProxyRequestEvent::class.qualifiedName
            processingData.classesForReflection shouldContain APIGatewayProxyResponseEvent::class.qualifiedName
            processingData.classesForReflection shouldContain "models.Person"
            processingData.classesForReflection shouldContain "models.NestedPerson"
        }
        compileStateService.status shouldBe Compilation.Status.SUCCESS
    }

    @Test
    fun correctlyProcessesHttpStoreFunctionAnnotationListNestedInput() {
        compileStateService.compileObjects {
            val classForReflectionService = ClassForReflectionService(processingData, it.typeUtils)
            httpFunctionResourceCreator = HttpFunctionResourceCreator(cfDocuments, processingData, classForReflectionService, it, setOf(), mockk(relaxed = true))

            val classElem = it.elementUtils.getTypeElement("handlers.HttpHandlers")
            val funcElem = classElem.enclosedElements[9]
            val results = httpFunctionResourceCreator.handleElement(funcElem, functionEnvironmentService)
            cfDocuments["dev"] shouldNotBe null

            val resources = cfDocuments["dev"]!!.updateTemplate.resources
            resources.size() shouldBe 9

            results.size shouldBe 1
            processingData.classesForReflection shouldContain APIGatewayProxyRequestEvent::class.qualifiedName
            processingData.classesForReflection shouldContain APIGatewayProxyResponseEvent::class.qualifiedName
            processingData.classesForReflection shouldContain "models.Person"
            processingData.classesForReflection shouldContain "models.NestedPerson"
            processingData.classesForReflection shouldContain "models.People"
        }
        compileStateService.status shouldBe Compilation.Status.SUCCESS
    }

    @Test
    fun correctlyProcessesHttpStoreFunctionAnnotationListNestedResponse() {
        compileStateService.compileObjects {
            val classForReflectionService = ClassForReflectionService(processingData, it.typeUtils)
            httpFunctionResourceCreator = HttpFunctionResourceCreator(cfDocuments, processingData, classForReflectionService, it, setOf(), mockk(relaxed = true))

            val classElem = it.elementUtils.getTypeElement("handlers.HttpHandlers")
            val funcElem = classElem.enclosedElements[10]
            val results = httpFunctionResourceCreator.handleElement(funcElem, functionEnvironmentService)
            cfDocuments["dev"] shouldNotBe null

            val resources = cfDocuments["dev"]!!.updateTemplate.resources
            resources.size() shouldBe 9

            results.size shouldBe 1
            processingData.classesForReflection shouldContain APIGatewayProxyRequestEvent::class.qualifiedName
            processingData.classesForReflection shouldContain APIGatewayProxyResponseEvent::class.qualifiedName
            processingData.classesForReflection shouldContain "models.Person"
            processingData.classesForReflection shouldContain "models.People"
        }
        compileStateService.status shouldBe Compilation.Status.SUCCESS
    }

}
