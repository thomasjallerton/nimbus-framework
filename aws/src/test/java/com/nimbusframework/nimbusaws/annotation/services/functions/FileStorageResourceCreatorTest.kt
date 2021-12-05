package com.nimbusframework.nimbusaws.annotation.services.functions

import com.amazonaws.services.lambda.runtime.events.S3Event
import com.google.testing.compile.Compilation
import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.annotation.processor.FunctionInformation
import com.nimbusframework.nimbusaws.annotation.processor.ProcessingData
import com.nimbusframework.nimbusaws.annotation.services.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.annotation.services.ResourceFinder
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.resource.file.FileBucket
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

class FileStorageResourceCreatorTest : AnnotationSpec() {

    private lateinit var fileStorageResourceCreator: FileStorageResourceCreator
    private lateinit var roundEnvironment: RoundEnvironment
    private lateinit var cfDocuments: MutableMap<String, CloudFormationFiles>
    private lateinit var processingData: ProcessingData
    private lateinit var functionEnvironmentService: FunctionEnvironmentService
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

        compileStateService = CompileStateService("handlers/FileStorageHandlers.java")

        functionEnvironmentService = FunctionEnvironmentService(cfDocuments, processingData.nimbusState)
    }

    @Test
    fun correctlyProcessesFileStorageFunctionAnnotation() {
        compileStateService.compileObjects {
            every { resourceFinder.getFileStorageBucketResource(any(), any(), any()) } returns FileBucket(processingData.nimbusState, "ImageBucket", arrayOf(), "dev" )

            fileStorageResourceCreator = FileStorageResourceCreator(cfDocuments, processingData, mockk(relaxed = true), it, setOf(), messager, resourceFinder)

            val classElem = it.elementUtils.getTypeElement("handlers.FileStorageHandlers")
            val funcElem = classElem.enclosedElements[1]
            val results = fileStorageResourceCreator.handleElement(funcElem, functionEnvironmentService)
            cfDocuments["dev"] shouldNotBe null

            val resources = cfDocuments["dev"]!!.updateTemplate.resources
            resources.size() shouldBe 5

            results.size shouldBe 1

            verify { messager wasNot Called }
            processingData.classesForReflection shouldContain S3Event::class.qualifiedName
        }

        compileStateService.status shouldBe Compilation.Status.SUCCESS
    }

    @Test
    fun logsErrorIfCantFindFileStorageBucket() {
        compileStateService.compileObjects {
            every { resourceFinder.getFileStorageBucketResource(any(), any(), any()) } returns null

            fileStorageResourceCreator = FileStorageResourceCreator(cfDocuments, processingData, mockk(relaxed = true), it, setOf(), messager, resourceFinder)

            val classElem = it.elementUtils.getTypeElement("handlers.FileStorageHandlers")
            val funcElem = classElem.enclosedElements[1]
            fileStorageResourceCreator.handleElement(funcElem, functionEnvironmentService)

            verify { messager.printMessage(Diagnostic.Kind.ERROR, any(), any()) }
            processingData.classesForReflection shouldContain S3Event::class.qualifiedName
        }

        compileStateService.status shouldBe Compilation.Status.SUCCESS
    }

}
