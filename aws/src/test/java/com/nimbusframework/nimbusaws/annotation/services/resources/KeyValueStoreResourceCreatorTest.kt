package com.nimbusframework.nimbusaws.annotation.services.resources

import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.resource.dynamo.DynamoResource
import com.nimbusframework.nimbuscore.persisted.NimbusState
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.mockk
import javax.annotation.processing.RoundEnvironment

class KeyValueStoreResourceCreatorTest : AnnotationSpec() {

    private lateinit var keyValueStoreResourceCreator: KeyValueStoreResourceCreator
    private lateinit var roundEnvironment: RoundEnvironment
    private lateinit var cfDocuments: MutableMap<String, CloudFormationFiles>
    private lateinit var nimbusState: NimbusState
    private lateinit var compileStateService: CompileStateService

    @BeforeEach
    fun setup() {
        nimbusState = NimbusState()
        cfDocuments = mutableMapOf()
        roundEnvironment = mockk()
        compileStateService = CompileStateService("models/KeyValue.java", "models/DynamoDbKeyValue.java", "models/KeyValueExistingArn.java")
    }

    @Test
    fun correctlyProcessesKeyValueAnnotation() {
        compileStateService.compileObjects {
            keyValueStoreResourceCreator = KeyValueStoreResourceCreator(roundEnvironment, cfDocuments, nimbusState, it)

            keyValueStoreResourceCreator.handleAgnosticType(it.elementUtils.getTypeElement("models.KeyValue"))
            cfDocuments["dev"] shouldNotBe null

            val resources = cfDocuments["dev"]!!.updateTemplate.resources
            resources.size() shouldBe 2

            val dynamoResource = resources.get("KeyValuedev") as DynamoResource

            dynamoResource shouldNotBe null
        }
    }

    @Test
    fun correctlyProcessesDynamoDbKeyValueAnnotation() {
        compileStateService.compileObjects {
            keyValueStoreResourceCreator = KeyValueStoreResourceCreator(roundEnvironment, cfDocuments, nimbusState, it)
            keyValueStoreResourceCreator.handleSpecificType(it.elementUtils.getTypeElement("models.DynamoDbKeyValue"))
            cfDocuments["dev"] shouldNotBe null

            val resources = cfDocuments["dev"]!!.updateTemplate.resources
            resources.size() shouldBe 2

            val dynamoResource = resources.get("keytabledev") as DynamoResource

            dynamoResource shouldNotBe null
        }
    }

    @Test
    fun doesNotCreateResourceIfExistingArnSet() {
        compileStateService.compileObjects {
            keyValueStoreResourceCreator = KeyValueStoreResourceCreator(roundEnvironment, cfDocuments, nimbusState, it)
            keyValueStoreResourceCreator.handleSpecificType(it.elementUtils.getTypeElement("models.KeyValueExistingArn"))
            cfDocuments["dev"] shouldNotBe null

            val resources = cfDocuments["dev"]!!.updateTemplate.resources
            resources.size() shouldBe 1

            resources.get("KeyValueExistingArndev") shouldBe null
        }
    }
}