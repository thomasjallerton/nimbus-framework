package com.nimbusframework.nimbusaws.annotation.services.resources

import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.annotation.processor.ProcessingData
import com.nimbusframework.nimbusaws.annotation.services.dependencies.ClassForReflectionService
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.resource.dynamo.DynamoResource
import com.nimbusframework.nimbuscore.persisted.NimbusState
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.mockk
import javax.annotation.processing.RoundEnvironment

class DocumentStoreResourceCreatorTest : AnnotationSpec() {

    private lateinit var roundEnvironment: RoundEnvironment
    private lateinit var cfDocuments: MutableMap<String, CloudFormationFiles>
    private lateinit var nimbusState: NimbusState
    private lateinit var processingData: ProcessingData
    private lateinit var compileState: CompileStateService

    @BeforeEach
    fun setup() {
        nimbusState = NimbusState()
        processingData = ProcessingData(nimbusState)
        cfDocuments = mutableMapOf()
        roundEnvironment = mockk()
        compileState = CompileStateService("models/Document.java", "models/DynamoDbDocument.java", "models/DocumentExistingArn.java")
    }

    @Test
    fun correctlyProcessesDocumentAnnotation() {
        compileState.compileObjects {
            val elements = it.elementUtils
            val documentStoreResourceCreator = DocumentStoreResourceCreator(roundEnvironment, cfDocuments, processingData, ClassForReflectionService(processingData, it.typeUtils))
            documentStoreResourceCreator.handleAgnosticType(elements.getTypeElement("models.Document"))
            cfDocuments["dev"] shouldNotBe null

            val resources = cfDocuments["dev"]!!.updateTemplate.resources
            resources.size() shouldBe 2

            val dynamoResource = resources.get("Documentdev") as DynamoResource

            dynamoResource shouldNotBe null

            processingData.classesForReflection shouldContain "models.Document"
        }
    }

    @Test
    fun correctlyProcessesDynamoDbDocumentAnnotation() {
        compileState.compileObjects {
            val elements = it.elementUtils
            val documentStoreResourceCreator = DocumentStoreResourceCreator(roundEnvironment, cfDocuments, processingData, ClassForReflectionService(processingData, it.typeUtils))
            documentStoreResourceCreator.handleSpecificType(elements.getTypeElement("models.DynamoDbDocument"))
            cfDocuments["dev"] shouldNotBe null

            val resources = cfDocuments["dev"]!!.updateTemplate.resources
            resources.size() shouldBe 2

            val dynamoResource = resources.get("doctabledev") as DynamoResource

            dynamoResource shouldNotBe null

            processingData.classesForReflection shouldContain "models.DynamoDbDocument"
        }
    }

    @Test
    fun doesNotCreateResourceIfExistingArnSet() {
        compileState.compileObjects {
            val elements = it.elementUtils
            val documentStoreResourceCreator = DocumentStoreResourceCreator(roundEnvironment, cfDocuments, processingData, ClassForReflectionService(processingData, it.typeUtils))
            documentStoreResourceCreator.handleSpecificType(elements.getTypeElement("models.DocumentExistingArn"))
            cfDocuments["dev"] shouldNotBe null

            val resources = cfDocuments["dev"]!!.updateTemplate.resources
            resources.size() shouldBe 1

            resources.get("DocumentExistingArndev") shouldBe null

            processingData.classesForReflection shouldContain "models.DocumentExistingArn"
        }
    }
}
