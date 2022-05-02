package com.nimbusframework.nimbusaws.annotation.services.resources

import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.annotation.processor.ProcessingData
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.ClassForReflectionService
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.dynamo.DocumentStoreResourceCreator
import com.nimbusframework.nimbusaws.cloudformation.model.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.model.resource.dynamo.DynamoResource
import com.nimbusframework.nimbuscore.persisted.NimbusState
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.collections.shouldContain
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
        nimbusState = NimbusState(customRuntime = true)
        processingData = ProcessingData(nimbusState)
        cfDocuments = mutableMapOf()
        roundEnvironment = mockk()
        compileState = CompileStateService("models/Document.java", "models/DynamoDbDocument.java")
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

}
