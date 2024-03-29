package com.nimbusframework.nimbusaws.cloudformation.generation.abstractions

import com.nimbusframework.nimbusaws.annotation.processor.ProcessingData
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.cloudformation.model.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.model.processing.FileBuilderMethodInformation
import com.nimbusframework.nimbusaws.cloudformation.model.resource.ResourceCollection
import com.nimbusframework.nimbusaws.cloudformation.model.resource.function.FunctionConfig
import com.nimbusframework.nimbuscore.persisted.HandlerInformation
import com.nimbusframework.nimbuscore.persisted.NimbusState
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.mockk

class FunctionEnvironmentServiceTest : AnnotationSpec() {

    private lateinit var underTest: FunctionEnvironmentService
    private lateinit var nimbusState: NimbusState
    private lateinit var cloudFormationFiles: MutableMap<String, CloudFormationFiles>
    private lateinit var createResources: ResourceCollection
    private lateinit var updateResources: ResourceCollection

    @BeforeEach
    fun setUp() {
        nimbusState = NimbusState()
        cloudFormationFiles = mutableMapOf(Pair("dev", CloudFormationFiles(nimbusState, "dev")))
        createResources = cloudFormationFiles["dev"]!!.createTemplate.resources
        updateResources = cloudFormationFiles["dev"]!!.updateTemplate.resources
        underTest = FunctionEnvironmentService(cloudFormationFiles, ProcessingData(nimbusState))
    }

    @Test
    fun correctlyCreatesAFunction() {
        val fileBuilderMethodInformation = FileBuilderMethodInformation("Test", null, "testMethod", "com.test", listOf(), mockk())
        val handlerInformation = HandlerInformation("", "testHandler", "")
        val functionConfig = FunctionConfig(10, 2000, "dev")

        underTest.newFunction(fileBuilderMethodInformation, handlerInformation, functionConfig)

        createResources.size() shouldBe 1
        updateResources.size() shouldBe 2

        updateResources.get("NimbusDeploymentBucket") shouldNotBe null
        updateResources.get("ctTesttestMethodFunction") shouldNotBe null
        updateResources.getFunction("com.test.Test", "testMethod") shouldNotBe null
    }

}
