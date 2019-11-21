package com.nimbusframework.nimbusaws.annotation.services

import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.processing.MethodInformation
import com.nimbusframework.nimbusaws.cloudformation.resource.ResourceCollection
import com.nimbusframework.nimbusaws.cloudformation.resource.function.FunctionConfig
import com.nimbusframework.nimbuscore.persisted.HandlerInformation
import com.nimbusframework.nimbuscore.persisted.NimbusState
import io.kotlintest.specs.AnnotationSpec
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
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
        underTest = FunctionEnvironmentService(cloudFormationFiles, nimbusState)
    }

    @Test
    fun correctlyCreatesAFunction() {
        val methodInformation = MethodInformation("Test", "testMethod", "com.test", listOf(), mockk())
        val handlerInformation = HandlerInformation()
        val functionConfig = FunctionConfig(10, 2000, "dev")

        underTest.newFunction("testHandler", methodInformation, handlerInformation, functionConfig)

        createResources.size() shouldBe 1
        updateResources.size() shouldBe 4

        updateResources.get("IamRoleTesttestMethodExecution") shouldNotBe null
        updateResources.get("NimbusDeploymentBucket") shouldNotBe null
        updateResources.get("TesttestMethodLogGroup") shouldNotBe null
        updateResources.get("TesttestMethodFunction") shouldNotBe null
    }

}