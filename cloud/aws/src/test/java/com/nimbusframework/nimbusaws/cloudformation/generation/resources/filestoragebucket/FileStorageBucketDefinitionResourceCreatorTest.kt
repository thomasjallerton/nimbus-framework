package com.nimbusframework.nimbusaws.cloudformation.generation.resources.filestoragebucket

import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.filestoragebucket.FileStorageBucketResourceCreator
import com.nimbusframework.nimbusaws.cloudformation.model.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.model.outputs.BucketWebsiteUrlOutput
import com.nimbusframework.nimbusaws.cloudformation.model.resource.file.FileBucketResource
import com.nimbusframework.nimbusaws.cloudformation.model.resource.file.FileStorageBucketPolicy
import com.nimbusframework.nimbuscore.persisted.NimbusState
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.mockk
import javax.annotation.processing.RoundEnvironment

class FileStorageBucketDefinitionResourceCreatorTest : AnnotationSpec() {

    private lateinit var fileStorageBucketResourceCreator: FileStorageBucketResourceCreator
    private lateinit var roundEnvironment: RoundEnvironment
    private lateinit var cfDocuments: MutableMap<String, CloudFormationFiles>
    private lateinit var nimbusState: NimbusState
    private lateinit var compileState: CompileStateService

    @BeforeEach
    fun setup() {
        nimbusState = NimbusState()
        cfDocuments = mutableMapOf()
        roundEnvironment = mockk()
        compileState = CompileStateService("models/FileStorage.java", "models/Website.java")
        fileStorageBucketResourceCreator = FileStorageBucketResourceCreator(roundEnvironment, cfDocuments, nimbusState)
    }

    @Test
    fun correctlyProcessesFileStorageBucketAnnotation() {
        compileState.compileObjects {
            val elements = it.elementUtils
            fileStorageBucketResourceCreator.handleAgnosticType(elements.getTypeElement("models.FileStorage"))
            cfDocuments["dev"] shouldNotBe null

            val resources = cfDocuments["dev"]!!.updateTemplate.resources
            resources.size() shouldBe 2

            val fileBucket = resources.get("ImageBucketFileBucket") as FileBucketResource

            fileBucket shouldNotBe null

            fileBucket.annotationBucketName shouldBe "ImageBucket"
        }
    }

    @Test
    fun correctlyProcessesFileStorageBucketAsWebsiteAnnotation() {
        compileState.compileObjects {
            val elements = it.elementUtils
            fileStorageBucketResourceCreator.handleAgnosticType(elements.getTypeElement("models.Website"))
            cfDocuments["dev"] shouldNotBe null

            val resources = cfDocuments["dev"]!!.updateTemplate.resources
            val outputs = cfDocuments["dev"]!!.updateTemplate.outputs
            resources.size() shouldBe 3

            val fileBucket = resources.get("websiteFileBucket") as FileBucketResource
            val fileBucketPolicy = resources.get("PolicywebsiteFileBucket") as FileStorageBucketPolicy
            val websiteUrlOutput = outputs.get("websiteFileBucketWebsiteUrl") as BucketWebsiteUrlOutput

            fileBucket shouldNotBe null
            fileBucketPolicy shouldNotBe null
            websiteUrlOutput shouldNotBe null

            fileBucket.annotationBucketName shouldBe "website"
        }
    }

}
