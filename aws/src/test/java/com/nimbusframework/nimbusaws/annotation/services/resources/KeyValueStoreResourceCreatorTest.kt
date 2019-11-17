package com.nimbusframework.nimbusaws.annotation.services.resources

import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.resource.dynamo.DynamoResource
import com.nimbusframework.nimbuscore.persisted.NimbusState
import io.kotlintest.specs.AnnotationSpec
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.mockk.mockk
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.util.Elements

class KeyValueStoreResourceCreatorTest : AnnotationSpec() {

    private lateinit var keyValueStoreResourceCreator: KeyValueStoreResourceCreator
    private lateinit var roundEnvironment: RoundEnvironment
    private lateinit var cfDocuments: MutableMap<String, CloudFormationFiles>
    private lateinit var nimbusState: NimbusState
    private lateinit var elements: Elements

    @BeforeEach
    fun setup() {
        nimbusState = NimbusState()
        cfDocuments = mutableMapOf()
        roundEnvironment = mockk()
        val compileState = CompileStateService("models/KeyValue.java", "models/DynamoDbKeyValue.java")
        elements = compileState.elements
        keyValueStoreResourceCreator = KeyValueStoreResourceCreator(roundEnvironment, cfDocuments, nimbusState, compileState.processingEnvironment)
    }

    @Test
    fun correctlyProcessesKeyValueAnnotation() {
        keyValueStoreResourceCreator.handleAgnosticType(elements.getTypeElement("models.KeyValue"))
        cfDocuments["dev"] shouldNotBe null

        val resources = cfDocuments["dev"]!!.updateTemplate.resources
        resources.size() shouldBe 2

        val dynamoResource = resources.get("KeyValuedev") as DynamoResource

        dynamoResource shouldNotBe null
    }

    @Test
    fun correctlyProcessesDynamoDbKeyValueAnnotation() {
        keyValueStoreResourceCreator.handleSpecificType(elements.getTypeElement("models.DynamoDbKeyValue"))
        cfDocuments["dev"] shouldNotBe null

        val resources = cfDocuments["dev"]!!.updateTemplate.resources
        resources.size() shouldBe 2

        val dynamoResource = resources.get("DynamoDbKeyValuedev") as DynamoResource

        dynamoResource shouldNotBe null
    }
}