package com.nimbusframework.nimbusaws.cloudformation.generation.resources.filestoragebucket

import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.annotation.processor.ProcessingData
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.ResourceFinder
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.filestoragebucket.FileUploadResourceCreator
import com.nimbusframework.nimbusaws.cloudformation.model.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.model.resource.file.FileBucketResource
import com.nimbusframework.nimbuscore.persisted.NimbusState
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import javax.annotation.processing.Messager
import javax.annotation.processing.RoundEnvironment
import javax.tools.Diagnostic

class FileUploadResourceCreatorTest : AnnotationSpec() {

    private lateinit var roundEnvironment: RoundEnvironment
    private lateinit var cfDocuments: MutableMap<String, CloudFormationFiles>
    private lateinit var processingData: ProcessingData
    private lateinit var functionEnvironmentService: FunctionEnvironmentService
    private lateinit var compileStateService: CompileStateService
    private lateinit var resourceFinder: ResourceFinder
    private lateinit var messager: Messager

    @BeforeEach
    fun setup() {
        processingData = ProcessingData(NimbusState())
        cfDocuments = mutableMapOf()
        roundEnvironment = mockk()
        resourceFinder = mockk()
        messager = mockk(relaxed = true)

        compileStateService = CompileStateService("models/FileStorage.java")
        functionEnvironmentService = FunctionEnvironmentService(cfDocuments, processingData)
    }

    @Test
    fun correctlyProcessesFileUploadAnnotation() {
        compileStateService.compileObjects {
            every { resourceFinder.getFileStorageBucketResource(any(), any(), any()) } returns FileBucketResource(processingData.nimbusState, "ImageBucket", arrayOf(), "dev" )

            val fileUploadResourceCreator = FileUploadResourceCreator(cfDocuments, processingData, it, setOf(), messager, resourceFinder)
            val classElem = it.elementUtils.getTypeElement("models.FileStorage")
            val results = fileUploadResourceCreator.handleElement(classElem, functionEnvironmentService)
            cfDocuments["dev"] shouldBe null

            results.size shouldBe 0

            val fileUploadDescription = processingData.nimbusState.fileUploads["dev"]!!["imagebucketdev"]!![0]
            fileUploadDescription.localFile shouldBe "test"
            fileUploadDescription.targetFile shouldBe "test"
            fileUploadDescription.fileUploadVariableSubstitutionFileRegex shouldBe ""

            verify { messager wasNot Called }
        }
    }

    @Test
    fun logsErrorIfCannotFindFileStorageBucket() {
        compileStateService.compileObjects {
            every { resourceFinder.getFileStorageBucketResource(any(), any(), any()) } returns null

            val fileUploadResourceCreator = FileUploadResourceCreator(cfDocuments, processingData, it, setOf(), messager, resourceFinder)
            val classElem = it.elementUtils.getTypeElement("models.FileStorage")
            fileUploadResourceCreator.handleElement(classElem, functionEnvironmentService)

            verify { messager.printMessage(Diagnostic.Kind.ERROR, any(), any()) }
        }
    }
}
