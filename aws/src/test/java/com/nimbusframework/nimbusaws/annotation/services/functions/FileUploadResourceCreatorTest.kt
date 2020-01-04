package com.nimbusframework.nimbusaws.annotation.services.functions

import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.annotation.processor.FunctionInformation
import com.nimbusframework.nimbusaws.annotation.services.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbuscore.persisted.NimbusState
import io.kotlintest.should
import io.kotlintest.specs.AnnotationSpec
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.mockk.mockk
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.util.Elements

class FileUploadResourceCreatorTest : AnnotationSpec() {

    private lateinit var roundEnvironment: RoundEnvironment
    private lateinit var cfDocuments: MutableMap<String, CloudFormationFiles>
    private lateinit var nimbusState: NimbusState
    private lateinit var functionEnvironmentService: FunctionEnvironmentService
    private lateinit var compileStateService: CompileStateService

    @BeforeEach
    fun setup() {
        nimbusState = NimbusState()
        cfDocuments = mutableMapOf()
        roundEnvironment = mockk()
        compileStateService = CompileStateService("models/FileStorage.java")
        functionEnvironmentService = FunctionEnvironmentService(cfDocuments, nimbusState)
    }

    @Test
    fun correctlyProcessesFileUploadAnnotation() {
        compileStateService.compileObjects {
            val fileUploadResourceCreator = FileUploadResourceCreator(cfDocuments, nimbusState, it)
            val results: MutableList<FunctionInformation> = mutableListOf()
            val classElem = it.elementUtils.getTypeElement("models.FileStorage")
            fileUploadResourceCreator.handleElement(classElem, functionEnvironmentService, results)
            cfDocuments["dev"] shouldBe null

            results.size shouldBe 0

            val fileUploadDescription = nimbusState.fileUploads["dev"]!!["imagebucketdev"]!![0]
            fileUploadDescription.localFile shouldBe "test"
            fileUploadDescription.targetFile shouldBe "test"
            fileUploadDescription.substituteVariables shouldBe false
        }
    }

}