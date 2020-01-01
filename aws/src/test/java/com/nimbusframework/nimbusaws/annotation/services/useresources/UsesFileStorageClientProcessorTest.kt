package com.nimbusframework.nimbusaws.annotation.services.useresources

import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.annotation.services.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.annotation.services.functions.HttpFunctionResourceCreator
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.resource.IamRoleResource
import com.nimbusframework.nimbusaws.cloudformation.resource.function.FunctionResource
import com.nimbusframework.nimbuscore.persisted.ClientType
import com.nimbusframework.nimbuscore.persisted.NimbusState
import io.kotlintest.shouldBe
import io.kotlintest.specs.AnnotationSpec
import io.mockk.mockk
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.util.Elements

class UsesFileStorageClientProcessorTest: AnnotationSpec() {

    private lateinit var usesFileStorageClientProcessor: UsesFileStorageClientProcessor
    private lateinit var roundEnvironment: RoundEnvironment
    private lateinit var cfDocuments: MutableMap<String, CloudFormationFiles>
    private lateinit var nimbusState: NimbusState
    private lateinit var elements: Elements
    private lateinit var functionResource: FunctionResource
    private lateinit var iamRoleResource: IamRoleResource

    @BeforeEach
    fun setup() {
        nimbusState = NimbusState()
        cfDocuments = mutableMapOf()
        roundEnvironment = mockk()

        val compileState = CompileStateService("handlers/UsesFileStorageClientHandler.java")
        elements = compileState.elements

        HttpFunctionResourceCreator(cfDocuments, nimbusState, compileState.processingEnvironment).handleElement(elements.getTypeElement("handlers.UsesFileStorageClientHandler").enclosedElements[1], FunctionEnvironmentService(cfDocuments, nimbusState), mutableListOf())

        functionResource = cfDocuments["dev"]!!.updateTemplate.resources.get("UsesFileStorageClientHandlerfuncFunction") as FunctionResource
        iamRoleResource = cfDocuments["dev"]!!.updateTemplate.resources.get("IamRoleageClientHandlerfunc") as IamRoleResource
        usesFileStorageClientProcessor = UsesFileStorageClientProcessor(cfDocuments, nimbusState)
    }

    @Test
    fun correctlySetsPermissions() {
        usesFileStorageClientProcessor.handleUseResources(elements.getTypeElement("handlers.UsesFileStorageClientHandler").enclosedElements[1], functionResource)
        val bucketResource = cfDocuments["dev"]!!.updateTemplate.resources.get("TestFileBucket")!!

        functionResource.usesClient(ClientType.FileStorage) shouldBe true
        functionResource.getStrEnvValue("NIMBUS_STAGE") shouldBe "dev"

        iamRoleResource.allows("s3:GetObject", bucketResource, "") shouldBe true
        iamRoleResource.allows("s3:DeleteObject", bucketResource, "") shouldBe true
        iamRoleResource.allows("s3:PutObject", bucketResource, "") shouldBe true
        iamRoleResource.allows("s3:GetObject", bucketResource, "/*") shouldBe true
        iamRoleResource.allows("s3:DeleteObject", bucketResource, "/*") shouldBe true
        iamRoleResource.allows("s3:PutObject", bucketResource, "/*") shouldBe true
    }

}