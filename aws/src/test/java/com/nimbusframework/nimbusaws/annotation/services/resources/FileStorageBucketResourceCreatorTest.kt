package com.nimbusframework.nimbusaws.annotation.services.resources

import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.outputs.BucketWebsiteUrlOutput
import com.nimbusframework.nimbusaws.cloudformation.resource.dynamo.DynamoResource
import com.nimbusframework.nimbusaws.cloudformation.resource.file.FileBucket
import com.nimbusframework.nimbusaws.cloudformation.resource.file.FileStorageBucketPolicy
import com.nimbusframework.nimbuscore.persisted.NimbusState
import io.kotlintest.should
import io.kotlintest.specs.AnnotationSpec
import io.kotlintest.shouldBe
import io.kotlintest.shouldNot
import io.kotlintest.shouldNotBe
import io.mockk.mockk
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.util.Elements

class FileStorageBucketResourceCreatorTest : AnnotationSpec() {

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

            val fileBucket = resources.get("ImageBucketFileBucket") as FileBucket

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

            val fileBucket = resources.get("websiteFileBucket") as FileBucket
            val fileBucketPolicy = resources.get("PolicywebsiteFileBucket") as FileStorageBucketPolicy
            val websiteUrlOutput = outputs.get("websiteFileBucketWebsiteUrl") as BucketWebsiteUrlOutput

            fileBucket shouldNotBe null
            fileBucketPolicy shouldNotBe null
            websiteUrlOutput shouldNotBe null

            fileBucket.annotationBucketName shouldBe "website"
        }
    }

}