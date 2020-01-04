package com.nimbusframework.nimbusaws.annotation.services.resources

import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.resource.database.RdsConfiguration
import com.nimbusframework.nimbusaws.cloudformation.resource.database.RdsResource
import com.nimbusframework.nimbusaws.cloudformation.resource.dynamo.DynamoResource
import com.nimbusframework.nimbuscore.annotations.database.DatabaseLanguage
import com.nimbusframework.nimbuscore.annotations.database.DatabaseSize
import com.nimbusframework.nimbuscore.persisted.NimbusState
import io.kotlintest.specs.AnnotationSpec
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.mockk.mockk
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.util.Elements

class DocumentStoreResourceCreatorTest : AnnotationSpec() {

    private lateinit var documentStoreResourceCreator: DocumentStoreResourceCreator
    private lateinit var roundEnvironment: RoundEnvironment
    private lateinit var cfDocuments: MutableMap<String, CloudFormationFiles>
    private lateinit var nimbusState: NimbusState
    private lateinit var compileState: CompileStateService

    @BeforeEach
    fun setup() {
        nimbusState = NimbusState()
        cfDocuments = mutableMapOf()
        roundEnvironment = mockk()
        compileState = CompileStateService("models/Document.java", "models/DynamoDbDocument.java", "models/DocumentExistingArn.java")
        documentStoreResourceCreator = DocumentStoreResourceCreator(roundEnvironment, cfDocuments, nimbusState)
    }

    @Test
    fun correctlyProcessesDocumentAnnotation() {
        compileState.compileObjects {
            val elements = it.elementUtils
            documentStoreResourceCreator.handleAgnosticType(elements.getTypeElement("models.Document"))
            cfDocuments["dev"] shouldNotBe null

            val resources = cfDocuments["dev"]!!.updateTemplate.resources
            resources.size() shouldBe 2

            val dynamoResource = resources.get("Documentdev") as DynamoResource

            dynamoResource shouldNotBe null
        }
    }

    @Test
    fun correctlyProcessesDynamoDbDocumentAnnotation() {
        compileState.compileObjects {
            val elements = it.elementUtils
            documentStoreResourceCreator.handleSpecificType(elements.getTypeElement("models.DynamoDbDocument"))
            cfDocuments["dev"] shouldNotBe null

            val resources = cfDocuments["dev"]!!.updateTemplate.resources
            resources.size() shouldBe 2

            val dynamoResource = resources.get("doctabledev") as DynamoResource

            dynamoResource shouldNotBe null
        }
    }

    @Test
    fun doesNotCreateResourceIfExistingArnSet() {
        compileState.compileObjects {
            val elements = it.elementUtils
            documentStoreResourceCreator.handleSpecificType(elements.getTypeElement("models.DocumentExistingArn"))
            cfDocuments["dev"] shouldNotBe null

            val resources = cfDocuments["dev"]!!.updateTemplate.resources
            resources.size() shouldBe 1

            resources.get("DocumentExistingArndev") shouldBe null
        }
    }
}