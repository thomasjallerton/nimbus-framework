package com.nimbusframework.nimbusaws.cloudformation.generation.resources.filestoragebucket

import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.annotation.processor.ProcessingData
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.ResourceFinder
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.apigateway.HttpFunctionResourceCreator
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.filestoragebucket.UsesFileStorageClientProcessor
import com.nimbusframework.nimbusaws.cloudformation.model.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.model.resource.IamRoleResource
import com.nimbusframework.nimbusaws.cloudformation.model.resource.file.FileBucketResource
import com.nimbusframework.nimbusaws.cloudformation.model.resource.function.FunctionResource
import com.nimbusframework.nimbuscore.persisted.NimbusState
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
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
    private lateinit var processingData: ProcessingData
    private lateinit var functionResource: FunctionResource
    private lateinit var compileStateService: CompileStateService
    private lateinit var messager: Messager
    private lateinit var resourceFinder: ResourceFinder

    @BeforeEach
    fun setup() {
        processingData = ProcessingData(NimbusState())
        cfDocuments = mutableMapOf()
        roundEnvironment = mockk()
        messager = mockk(relaxed = true)
        resourceFinder = mockk()

        compileStateService = CompileStateService("models/FileStorage.java", "handlers/UsesFileStorageClientHandler.java")
    }

    private fun setup(processingEnvironment: ProcessingEnvironment, toRun: () -> Unit ) {
        val elements = processingEnvironment.elementUtils

        HttpFunctionResourceCreator(cfDocuments, processingData, mockk(relaxed = true), processingEnvironment, setOf(), mockk(relaxed = true)).handleElement(elements.getTypeElement("handlers.UsesFileStorageClientHandler").enclosedElements[1], FunctionEnvironmentService(cfDocuments, processingData))

        functionResource = cfDocuments["dev"]!!.updateTemplate.resources.get("hUsesFileStoraClientHandlerfuncFunction") as FunctionResource
        usesFileStorageClientProcessor = UsesFileStorageClientProcessor(messager, resourceFinder, processingData.nimbusState)

        toRun()
    }

    @Test
    fun correctlySetsPermissions() {
        compileStateService.compileObjects {
            setup(it) {
                val bucketResource = FileBucketResource(processingData.nimbusState, "ImageBucket", arrayOf(), "dev" )
                every { resourceFinder.getFileStorageBucketResource(any(), any(), any()) } returns bucketResource

                usesFileStorageClientProcessor.handleUseResources(it.elementUtils.getTypeElement("handlers.UsesFileStorageClientHandler").enclosedElements[1], functionResource)

                functionResource.getStrEnvValue("NIMBUS_STAGE") shouldBe "dev"

                functionResource.iamRoleResource.allows("s3:GetObject", bucketResource, "") shouldBe true
                functionResource.iamRoleResource.allows("s3:DeleteObject", bucketResource, "") shouldBe true
                functionResource.iamRoleResource.allows("s3:PutObject", bucketResource, "") shouldBe true
                functionResource.iamRoleResource.allows("s3:GetObject", bucketResource, "/*") shouldBe true
                functionResource.iamRoleResource.allows("s3:DeleteObject", bucketResource, "/*") shouldBe true
                functionResource.iamRoleResource.allows("s3:PutObject", bucketResource, "/*") shouldBe true

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
