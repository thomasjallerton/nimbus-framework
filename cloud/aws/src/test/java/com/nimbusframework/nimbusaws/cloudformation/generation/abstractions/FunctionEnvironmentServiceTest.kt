package com.nimbusframework.nimbusaws.cloudformation.generation.abstractions

import com.nimbusframework.nimbusaws.annotation.processor.ProcessingData
import com.nimbusframework.nimbusaws.cloudformation.model.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.model.processing.FileBuilderMethodInformation
import com.nimbusframework.nimbusaws.cloudformation.model.resource.ResourceCollection
import com.nimbusframework.nimbusaws.cloudformation.model.resource.file.FileBucketResource
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

        createResources.toJson()
        updateResources.toJson()

        createResources.size() shouldBe 1
        // 1 function, 1 IAM role, 1 log group, 1 deployment bucket
        updateResources.size() shouldBe 4

        updateResources.get("NimbusDeploymentBucket") shouldNotBe null
        updateResources.get("ctTesttestMethodFunction") shouldNotBe null
        updateResources.getFunction("com.test.Test", "testMethod") shouldNotBe null
    }

    @Test
    fun deduplicatesIamRolesForFunctionsAndCreatesSingleLogGroup() {
        val fileBuilderMethodInformation1 = FileBuilderMethodInformation("Test", null, "testMethod1", "com.test", listOf(), mockk())
        val fileBuilderMethodInformation2 = FileBuilderMethodInformation("Test", null, "testMethod2", "com.test", listOf(), mockk())
        val fileBuilderMethodInformation3 = FileBuilderMethodInformation("Test", null, "testMethod3", "com.test", listOf(), mockk())
        val handlerInformation = HandlerInformation("", "testHandler", "")
        val functionConfig = FunctionConfig(10, 2000, "dev")

        val functionResource1 = underTest.newFunction(fileBuilderMethodInformation1, handlerInformation, functionConfig)
        val functionResource2 = underTest.newFunction(fileBuilderMethodInformation2, handlerInformation, functionConfig)
        val functionResource3 = underTest.newFunction(fileBuilderMethodInformation3, handlerInformation, functionConfig)

        val fakeResource = FileBucketResource(nimbusState, "bucketName", arrayOf(), "dev")
        functionResource2.iamRoleResource.addAllowStatement("PutLogs", fakeResource, ":*")

        createResources.toJson()
        updateResources.toJson()

        createResources.size() shouldBe 1

        // 3 functions, 2 IAM roles, 1 log group, 1 deployment bucket
        updateResources.size() shouldBe 7

        updateResources.get(functionResource1.iamRoleResource.getName()) shouldNotBe null
        updateResources.get(functionResource2.iamRoleResource.getName()) shouldNotBe null
        updateResources.get(functionResource2.logGroupResource.getName()) shouldNotBe null

        functionResource1.iamRoleResource.getName() shouldBe functionResource3.iamRoleResource.getName()
        functionResource1.logGroupResource.getName() shouldBe functionResource2.logGroupResource.getName()
        functionResource1.iamRoleResource.getName() shouldNotBe functionResource2.iamRoleResource.getName()
    }

}
