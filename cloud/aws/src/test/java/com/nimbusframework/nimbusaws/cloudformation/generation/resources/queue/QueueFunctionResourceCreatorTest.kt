package com.nimbusframework.nimbusaws.cloudformation.generation.resources.queue

import com.amazonaws.services.lambda.runtime.events.SQSEvent
import com.google.testing.compile.Compilation
import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.annotation.annotations.parsed.ParsedQueueDefinition
import com.nimbusframework.nimbusaws.annotation.processor.ProcessingData
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.ResourceFinder
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.ClassForReflectionService
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.queue.QueueFunctionResourceCreator
import com.nimbusframework.nimbusaws.cloudformation.model.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.model.resource.queue.QueueResource
import com.nimbusframework.nimbuscore.persisted.NimbusState
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import javax.annotation.processing.Messager
import javax.annotation.processing.RoundEnvironment
import javax.tools.Diagnostic

class QueueFunctionResourceCreatorTest : AnnotationSpec() {

    private lateinit var queueFunctionResourceCreator: QueueFunctionResourceCreator
    private lateinit var roundEnvironment: RoundEnvironment
    private lateinit var cfDocuments: MutableMap<String, CloudFormationFiles>
    private lateinit var processingData: ProcessingData
    private lateinit var functionEnvironmentService: FunctionEnvironmentService
    private lateinit var compileStateService: CompileStateService
    private lateinit var messager: Messager
    private lateinit var resourceFinder: ResourceFinder

    @BeforeEach
    fun setup() {
        processingData = ProcessingData(NimbusState(customRuntime = true))
        cfDocuments = mutableMapOf()
        roundEnvironment = mockk()
        messager = mockk(relaxed = true)
        resourceFinder = mockk()

        compileStateService = CompileStateService("handlers/QueueHandlers.java")
        functionEnvironmentService = FunctionEnvironmentService(cfDocuments, processingData)
    }

    @Test
    fun correctlyProcessesQueueFunctionAnnotation() {
        compileStateService.compileObjects {
            every { resourceFinder.getQueueResource(any(), any(), any()) } returns QueueResource(ParsedQueueDefinition("messageQueue", 10), processingData.nimbusState, "dev")
            val classForReflectionService = ClassForReflectionService(processingData, it.typeUtils)
            queueFunctionResourceCreator = QueueFunctionResourceCreator(cfDocuments, processingData, it, classForReflectionService, setOf(), messager, resourceFinder)

            val classElem = it.elementUtils.getTypeElement("handlers.QueueHandlers")
            val funcElem = classElem.enclosedElements[1]
            val results = queueFunctionResourceCreator.handleElement(funcElem, functionEnvironmentService)
            cfDocuments["dev"] shouldNotBe null

            val resources = cfDocuments["dev"]!!.updateTemplate.resources
            resources.size() shouldBe 3

            results.size shouldBe 1

            verify { messager wasNot Called }
            processingData.classesForReflection shouldContain SQSEvent::class.qualifiedName
        }

        compileStateService.status shouldBe Compilation.Status.SUCCESS
    }

    @Test
    fun logsErrorWhenCannotFindQueue() {
        compileStateService.compileObjects {
            every { resourceFinder.getQueueResource(any(), any(), any()) } returns null
            val classForReflectionService = ClassForReflectionService(processingData, it.typeUtils)
            queueFunctionResourceCreator = QueueFunctionResourceCreator(cfDocuments, processingData, it, classForReflectionService, setOf(), messager, resourceFinder)

            val classElem = it.elementUtils.getTypeElement("handlers.QueueHandlers")
            val funcElem = classElem.enclosedElements[1]
            queueFunctionResourceCreator.handleElement(funcElem, functionEnvironmentService)

            verify { messager.printMessage(Diagnostic.Kind.ERROR, any(), any()) }
            processingData.classesForReflection shouldContain SQSEvent::class.qualifiedName
        }
    }

}
