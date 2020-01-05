package com.nimbusframework.nimbusaws.annotation.services.useresources

import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.annotation.services.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.annotation.services.ResourceFinder
import com.nimbusframework.nimbusaws.annotation.services.functions.HttpFunctionResourceCreator
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.resource.IamRoleResource
import com.nimbusframework.nimbusaws.cloudformation.resource.file.FileBucket
import com.nimbusframework.nimbusaws.cloudformation.resource.function.FunctionResource
import com.nimbusframework.nimbuscore.persisted.ClientType
import com.nimbusframework.nimbuscore.persisted.NimbusState
import io.kotlintest.shouldBe
import io.kotlintest.specs.AnnotationSpec
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.tools.Diagnostic

class UsesFileStorageBucketClientProcessorTest: AnnotationSpec() {

    private lateinit var usesFileStorageClientProcessor: UsesFileStorageClientProcessor
    private lateinit var roundEnvironment: RoundEnvironment
    private lateinit var cfDocuments: MutableMap<String, CloudFormationFiles>
    private lateinit var nimbusState: NimbusState
    private lateinit var functionResource: FunctionResource
    private lateinit var iamRoleResource: IamRoleResource
    private lateinit var compileStateService: CompileStateService
    private lateinit var messager: Messager
    private lateinit var resourceFinder: ResourceFinder

    @BeforeEach
    fun setup() {
        nimbusState = NimbusState()
        cfDocuments = mutableMapOf()
        roundEnvironment = mockk()
        messager = mockk(relaxed = true)
        resourceFinder = mockk()

        compileStateService = CompileStateService("models/FileStorage.java", "handlers/UsesFileStorageClientHandler.java")
    }

    private fun setup(processingEnvironment: ProcessingEnvironment, toRun: () -> Unit ) {
        val elements = processingEnvironment.elementUtils

        HttpFunctionResourceCreator(cfDocuments, nimbusState, processingEnvironment).handleElement(elements.getTypeElement("handlers.UsesFileStorageClientHandler").enclosedElements[1], FunctionEnvironmentService(cfDocuments, nimbusState), mutableListOf())

        functionResource = cfDocuments["dev"]!!.updateTemplate.resources.get("UsesFileStorageClientHandlerfuncFunction") as FunctionResource
        iamRoleResource = cfDocuments["dev"]!!.updateTemplate.resources.get("IamRoleageClientHandlerfunc") as IamRoleResource
        usesFileStorageClientProcessor = UsesFileStorageClientProcessor(cfDocuments, messager, resourceFinder)

        toRun()
    }

    @Test
    fun correctlySetsPermissions() {
        compileStateService.compileObjects {
            setup(it) {
                val bucketResource = FileBucket(nimbusState, "ImageBucket", arrayOf(), "dev" )
                every { resourceFinder.getFileStorageBucketResource(any(), any(), any()) } returns bucketResource

                usesFileStorageClientProcessor.handleUseResources(it.elementUtils.getTypeElement("handlers.UsesFileStorageClientHandler").enclosedElements[1], functionResource)

                functionResource.usesClient(ClientType.FileStorage) shouldBe true
                functionResource.getStrEnvValue("NIMBUS_STAGE") shouldBe "dev"

                iamRoleResource.allows("s3:GetObject", bucketResource, "") shouldBe true
                iamRoleResource.allows("s3:DeleteObject", bucketResource, "") shouldBe true
                iamRoleResource.allows("s3:PutObject", bucketResource, "") shouldBe true
                iamRoleResource.allows("s3:GetObject", bucketResource, "/*") shouldBe true
                iamRoleResource.allows("s3:DeleteObject", bucketResource, "/*") shouldBe true
                iamRoleResource.allows("s3:PutObject", bucketResource, "/*") shouldBe true

                verify { messager wasNot Called }
            }
        }
    }

    @Test
    fun logsErrorIfCannotFindFileStorageBucket() {
        compileStateService.compileObjects {
            setup(it) {
                every { resourceFinder.getFileStorageBucketResource(any(), any(), any()) } returns null

                usesFileStorageClientProcessor.handleUseResources(it.elementUtils.getTypeElement("handlers.UsesFileStorageClientHandler").enclosedElements[1], functionResource)

                verify { messager.printMessage(Diagnostic.Kind.ERROR, any(), any()) }
            }
        }
    }

}