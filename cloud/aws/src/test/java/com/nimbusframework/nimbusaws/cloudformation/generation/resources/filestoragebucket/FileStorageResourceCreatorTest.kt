package com.nimbusframework.nimbusaws.cloudformation.generation.resources.filestoragebucket

import com.amazonaws.services.lambda.runtime.events.S3Event
import com.google.testing.compile.Compilation
import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.annotation.processor.ProcessingData
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.ResourceFinder
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.ClassForReflectionService
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.filestoragebucket.FileStorageResourceCreator
import com.nimbusframework.nimbusaws.cloudformation.model.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.model.resource.file.FileBucketResource
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
        processingData = ProcessingData(NimbusState(customRuntime = true))
        cfDocuments = mutableMapOf()
        roundEnvironment = mockk()
        messager = mockk(relaxed = true)
        resourceFinder = mockk()

        compileStateService = CompileStateService("handlers/FileStorageHandlers.java")

        functionEnvironmentService = FunctionEnvironmentService(cfDocuments, processingData)
    }

    @Test
    fun correctlyProcessesFileStorageFunctionAnnotation() {
        compileStateService.compileObjects {
            every { resourceFinder.getFileStorageBucketResource(any(), any(), any()) } returns FileBucketResource(processingData.nimbusState, "ImageBucket", arrayOf(), "dev" )
            val classForReflectionService = ClassForReflectionService(processingData, it.typeUtils)

            fileStorageResourceCreator = FileStorageResourceCreator(cfDocuments, processingData, classForReflectionService, it, setOf(), messager, resourceFinder)

            val classElem = it.elementUtils.getTypeElement("handlers.FileStorageHandlers")
            val funcElem = classElem.enclosedElements[1]
            val results = fileStorageResourceCreator.handleElement(funcElem, functionEnvironmentService)
            cfDocuments["dev"] shouldNotBe null

            val resources = cfDocuments["dev"]!!.updateTemplate.resources
            resources.size() shouldBe 3

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
            val classForReflectionService = ClassForReflectionService(processingData, it.typeUtils)

            fileStorageResourceCreator = FileStorageResourceCreator(cfDocuments, processingData, classForReflectionService, it, setOf(), messager, resourceFinder)

            val classElem = it.elementUtils.getTypeElement("handlers.FileStorageHandlers")
            val funcElem = classElem.enclosedElements[1]
            fileStorageResourceCreator.handleElement(funcElem, functionEnvironmentService)

            verify { messager.printMessage(Diagnostic.Kind.ERROR, any(), any()) }
            processingData.classesForReflection shouldContain S3Event::class.qualifiedName
        }

        compileStateService.status shouldBe Compilation.Status.SUCCESS
    }

}
