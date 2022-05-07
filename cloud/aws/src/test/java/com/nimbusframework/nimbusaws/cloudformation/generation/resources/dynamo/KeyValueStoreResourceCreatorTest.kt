package com.nimbusframework.nimbusaws.cloudformation.generation.resources.dynamo

import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.annotation.processor.ProcessingData
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.ClassForReflectionService
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.dynamo.KeyValueStoreResourceCreator
import com.nimbusframework.nimbusaws.cloudformation.model.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.model.resource.dynamo.DynamoResource
import com.nimbusframework.nimbuscore.persisted.NimbusState
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.mockk
import javax.annotation.processing.RoundEnvironment

class KeyValueStoreResourceCreatorTest : AnnotationSpec() {

    private lateinit var roundEnvironment: RoundEnvironment
    private lateinit var cfDocuments: MutableMap<String, CloudFormationFiles>
    private lateinit var nimbusState: NimbusState
    private lateinit var processingData: ProcessingData
    private lateinit var compileStateService: CompileStateService

    @BeforeEach
    fun setup() {
        nimbusState = NimbusState(customRuntime = true)
        processingData = ProcessingData(nimbusState)
        cfDocuments = mutableMapOf()
        roundEnvironment = mockk()
        compileStateService = CompileStateService("models/KeyValue.java", "models/DynamoDbKeyValue.java")
    }

    @Test
    fun correctlyProcessesKeyValueAnnotation() {
        compileStateService.compileObjects {
            val keyValueStoreResourceCreator = KeyValueStoreResourceCreator(roundEnvironment, cfDocuments, processingData, ClassForReflectionService(processingData, it.typeUtils), it)
            keyValueStoreResourceCreator.handleAgnosticType(it.elementUtils.getTypeElement("models.KeyValue"))
            cfDocuments["dev"] shouldNotBe null

            val resources = cfDocuments["dev"]!!.updateTemplate.resources
            resources.size() shouldBe 2

            val dynamoResource = resources.get("KeyValuedev") as DynamoResource

            dynamoResource shouldNotBe null
            processingData.classesForReflection shouldContain "models.KeyValue"
        }
    }

    @Test
    fun correctlyProcessesDynamoDbKeyValueAnnotation() {
        compileStateService.compileObjects {
            val keyValueStoreResourceCreator = KeyValueStoreResourceCreator(roundEnvironment, cfDocuments, processingData, ClassForReflectionService(processingData, it.typeUtils), it)
            keyValueStoreResourceCreator.handleSpecificType(it.elementUtils.getTypeElement("models.DynamoDbKeyValue"))
            cfDocuments["dev"] shouldNotBe null

            val resources = cfDocuments["dev"]!!.updateTemplate.resources
            resources.size() shouldBe 2

            val dynamoResource = resources.get("keytabledev") as DynamoResource

            dynamoResource shouldNotBe null
            processingData.classesForReflection shouldContain "models.DynamoDbKeyValue"
        }
    }

}
