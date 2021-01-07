package com.nimbusframework.nimbusaws.annotation.services.functions

import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.annotation.processor.FunctionInformation
import com.nimbusframework.nimbusaws.annotation.services.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.annotation.services.ResourceFinder
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.resource.dynamo.DynamoResource
import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbuscore.wrappers.DynamoConfiguration
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import javax.annotation.processing.RoundEnvironment

class KeyValueStoreFunctionResourceCreatorTest : AnnotationSpec() {

    private lateinit var keyValueStoreFunctionResourceCreator: KeyValueStoreFunctionResourceCreator
    private lateinit var roundEnvironment: RoundEnvironment
    private lateinit var cfDocuments: MutableMap<String, CloudFormationFiles>
    private lateinit var nimbusState: NimbusState
    private lateinit var functionEnvironmentService: FunctionEnvironmentService
    private lateinit var resourceFinder: ResourceFinder
    private lateinit var compileStateService: CompileStateService

    @BeforeEach
    fun setup() {
        nimbusState = NimbusState()
        cfDocuments = mutableMapOf()
        roundEnvironment = mockk()
        resourceFinder = mockk()

        compileStateService = CompileStateService("handlers/KeyValueStoreHandlers.java")

        functionEnvironmentService = FunctionEnvironmentService(cfDocuments, nimbusState)
    }

    @Test
    fun correctlyProcessesKeyValueStoreFunctionAnnotation() {
        compileStateService.compileObjects {
            keyValueStoreFunctionResourceCreator = KeyValueStoreFunctionResourceCreator(cfDocuments, nimbusState, it, mockk(relaxed = true), resourceFinder)

            every { resourceFinder.getKeyValueStoreResource(any(), any(), any()) } returns DynamoResource(DynamoConfiguration("table"), nimbusState, "dev")
            val classElem = it.elementUtils.getTypeElement("handlers.KeyValueStoreHandlers")
            val funcElem = classElem.enclosedElements[1]
            val results = keyValueStoreFunctionResourceCreator.handleElement(funcElem, functionEnvironmentService)
            cfDocuments["dev"] shouldNotBe null

            val resources = cfDocuments["dev"]!!.updateTemplate.resources
            resources.size() shouldBe 5

            results.size shouldBe 1
        }
    }

}