package com.nimbusframework.nimbusaws.cloudformation.generation.resources.dynamo

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent
import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.annotation.processor.ProcessingData
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.ResourceFinder
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.ClassForReflectionService
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.dynamo.KeyValueStoreFunctionResourceCreator
import com.nimbusframework.nimbusaws.cloudformation.model.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.model.resource.dynamo.DynamoResource
import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbuscore.wrappers.DynamoConfiguration
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import javax.annotation.processing.RoundEnvironment

class KeyValueStoreFunctionResourceCreatorTest : AnnotationSpec() {

    private lateinit var keyValueStoreFunctionResourceCreator: KeyValueStoreFunctionResourceCreator
    private lateinit var roundEnvironment: RoundEnvironment
    private lateinit var cfDocuments: MutableMap<String, CloudFormationFiles>
    private lateinit var processingData: ProcessingData
    private lateinit var functionEnvironmentService: FunctionEnvironmentService
    private lateinit var resourceFinder: ResourceFinder
    private lateinit var compileStateService: CompileStateService

    @BeforeEach
    fun setup() {
        processingData = ProcessingData(NimbusState(customRuntime = true))
        cfDocuments = mutableMapOf()
        roundEnvironment = mockk()
        resourceFinder = mockk()

        compileStateService = CompileStateService("handlers/KeyValueStoreHandlers.java")

        functionEnvironmentService = FunctionEnvironmentService(cfDocuments, processingData)
    }

    @Test
    fun correctlyProcessesKeyValueStoreFunctionAnnotation() {
        compileStateService.compileObjects {
            val classForReflectionService = ClassForReflectionService(processingData, it.typeUtils)
            keyValueStoreFunctionResourceCreator = KeyValueStoreFunctionResourceCreator(cfDocuments, processingData, classForReflectionService, it, setOf(), mockk(relaxed = true), resourceFinder)

            every { resourceFinder.getKeyValueStoreResource(any(), any(), any()) } returns DynamoResource(DynamoConfiguration("table"), processingData.nimbusState, "dev")
            val classElem = it.elementUtils.getTypeElement("handlers.KeyValueStoreHandlers")
            val funcElem = classElem.enclosedElements[1]
            val results = keyValueStoreFunctionResourceCreator.handleElement(funcElem, functionEnvironmentService)
            cfDocuments["dev"] shouldNotBe null

            val resources = cfDocuments["dev"]!!.updateTemplate.resources
            resources.size() shouldBe 3

            results.size shouldBe 1
            processingData.classesForReflection shouldContain DynamodbEvent::class.qualifiedName
        }
    }

}
