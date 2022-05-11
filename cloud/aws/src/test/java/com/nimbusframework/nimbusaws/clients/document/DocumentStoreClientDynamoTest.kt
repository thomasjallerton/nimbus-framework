package com.nimbusframework.nimbusaws.clients.document

import com.nimbusframework.nimbusaws.clients.dynamo.DynamoClient
import com.nimbusframework.nimbusaws.clients.dynamo.DynamoHelper.numAttribute
import com.nimbusframework.nimbusaws.clients.dynamo.DynamoHelper.strAttribute
import com.nimbusframework.nimbusaws.examples.document.DocumentStoreNoTableName
import com.nimbusframework.nimbusaws.examples.document.KotlinDocumentStoreNoTableName
import com.nimbusframework.nimbuscore.clients.JacksonClient
import com.nimbusframework.nimbuscore.clients.store.ReadItemRequest
import com.nimbusframework.nimbuscore.clients.store.WriteItemRequest
import com.nimbusframework.nimbuscore.clients.store.conditions.Condition
import com.nimbusframework.nimbuscore.clients.store.conditions.function.AttributeExists
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import java.lang.reflect.Field

class DocumentStoreClientDynamoTest : AnnotationSpec() {

    private lateinit var underTest: DocumentStoreClientDynamo<DocumentStoreNoTableName>
    private lateinit var dynamoClient: DynamoClient

    private val attributes: Map<String, Field>
    private val exampleObj = mutableMapOf(Pair("string", strAttribute("test")), Pair("integer", numAttribute("17")))
    private val exampleKey = mapOf(Pair("string", strAttribute("test")))
    private val obj =
        DocumentStoreNoTableName("test", 17)

    init {
        val attributesMutable: MutableMap<String, Field> = mutableMapOf()
        attributesMutable["string"] = DocumentStoreNoTableName::class.java.getDeclaredField("string")
        attributesMutable["integer"] = DocumentStoreNoTableName::class.java.getDeclaredField("integer")
        attributes = attributesMutable
    }

    @BeforeEach
    fun setUp() {
        dynamoClient = mockk(relaxed = true)
        every { dynamoClient.toAttributeValue("test") } returns strAttribute("test")
        underTest = DocumentStoreClientDynamo(DocumentStoreNoTableName::class.java, "tableName", "dev") { dynamoClient }
    }

    @Test
    fun canPutItem() {
        underTest.put(obj)

        verify { dynamoClient.put(obj, attributes) }
    }

    @Test
    fun canPutItemWithCondition() {
        underTest.put(obj, AttributeExists("string"))

        verify { dynamoClient.put(obj, attributes, any(), AttributeExists("string")) }
    }

    @Test
    fun canDeleteObj() {
        underTest.delete(obj)

        verify { dynamoClient.deleteKey(eq(exampleKey)) }
    }

    @Test
    fun canDeleteObjWithCondition() {
        underTest.delete(obj, AttributeExists("string"))

        verify { dynamoClient.deleteKey(eq(exampleKey), eq(AttributeExists("string"))) }
    }

    @Test
    fun canDeleteKey() {
        underTest.deleteKey("test")

        verify { dynamoClient.deleteKey(eq(exampleKey)) }
    }

    @Test
    fun canDeleteKeyWithCondition() {
        underTest.deleteKey("test", AttributeExists("string"))

        verify { dynamoClient.deleteKey(eq(exampleKey), eq(AttributeExists("string"))) }
    }

    @Test
    fun canGetAll() {
        every { dynamoClient.getAll() } returns listOf(exampleObj)

        underTest.getAll() shouldContain obj
    }

    @Test
    fun canFilter() {
        val condition = mockk<Condition>()
        every { dynamoClient.filter(condition) } returns listOf(exampleObj)

        underTest.filter(condition) shouldContain obj
    }

    @Test
    fun canGetItem() {
        every { dynamoClient.get(exampleKey) } returns exampleObj

        underTest.get("test") shouldBe obj
    }

    @Test
    fun canGetKotlinItem() {
        val underTest = DocumentStoreClientDynamo(KotlinDocumentStoreNoTableName::class.java, "tableName", "dev") { dynamoClient }
        val kotlinObj = KotlinDocumentStoreNoTableName("test", listOf(obj))

        val exampleObj = mutableMapOf(
            Pair("string", strAttribute("test")),
            Pair("documents", AttributeValue.builder().s(JacksonClient.writeValueAsString(listOf(obj))).build())
        )

        every { dynamoClient.get(exampleKey) } returns exampleObj

        underTest.get("test") shouldBe kotlinObj
    }

    @Test
    fun canGetReadItem() {
        val readItemRequest = ReadItemRequest<DocumentStoreNoTableName>()
        every { dynamoClient.getReadItem<DocumentStoreNoTableName>(exampleKey, captureLambda()) } returns readItemRequest

        underTest.getReadItem("test") shouldBe readItemRequest
    }

    @Test
    fun canGetWriteItem() {
        val writeItemRequest = WriteItemRequest()
        every { dynamoClient.getWriteItem(obj, attributes) } returns writeItemRequest

        underTest.getWriteItem(obj) shouldBe writeItemRequest
    }

    @Test
    fun canGetWriteItemWithCondition() {
        val writeItemRequest = WriteItemRequest()
        every { dynamoClient.getWriteItem(obj, attributes, mapOf(), AttributeExists("string")) } returns writeItemRequest

        underTest.getWriteItem(obj, AttributeExists("string")) shouldBe writeItemRequest
    }

    @Test
    fun canGetIncrementUpdateRequest() {
        val writeItemRequest = WriteItemRequest()
        every { dynamoClient.getUpdateValueRequest(exampleKey, "integer", 10, "+") } returns writeItemRequest
        underTest.getIncrementValueRequest("test", "integer", 10) shouldBe writeItemRequest
    }

    @Test
    fun canGetIncrementUpdateRequestWithCondition() {
        val writeItemRequest = WriteItemRequest()
        every { dynamoClient.getUpdateValueRequest(exampleKey, "integer", 10, "+", AttributeExists("string")) } returns writeItemRequest
        underTest.getIncrementValueRequest("test", "integer", 10, AttributeExists("string")) shouldBe writeItemRequest
    }

    @Test
    fun canGetDecrementUpdateRequest() {
        val writeItemRequest = WriteItemRequest()
        every { dynamoClient.getUpdateValueRequest(exampleKey, "integer", 10, "-") } returns writeItemRequest
        underTest.getDecrementValueRequest("test", "integer", 10) shouldBe writeItemRequest
    }

    @Test
    fun canGetDecrementUpdateRequestWithCondition() {
        val writeItemRequest = WriteItemRequest()
        every { dynamoClient.getUpdateValueRequest(exampleKey, "integer", 10, "-", AttributeExists("string")) } returns writeItemRequest
        underTest.getDecrementValueRequest("test", "integer", 10, AttributeExists("string")) shouldBe writeItemRequest
    }

    @Test
    fun canGetDeleteKeyItemRequest() {
        val writeItemRequest = WriteItemRequest()
        every { dynamoClient.getDeleteRequest(exampleKey) } returns writeItemRequest
        underTest.getDeleteKeyItemRequest("test") shouldBe writeItemRequest
    }

    @Test
    fun canGetDeleteKeyItemRequestWithCondition() {
        val writeItemRequest = WriteItemRequest()
        every { dynamoClient.getDeleteRequest(exampleKey, AttributeExists("string")) } returns writeItemRequest
        underTest.getDeleteKeyItemRequest("test", AttributeExists("string")) shouldBe writeItemRequest
    }

    @Test
    fun canGetDeleteObjItemRequest() {
        val writeItemRequest = WriteItemRequest()
        every { dynamoClient.getDeleteRequest(exampleKey) } returns writeItemRequest
        underTest.getDeleteItemRequest(obj) shouldBe writeItemRequest
    }

    @Test
    fun canGetDeleteObjItemRequestWithCondition() {
        val writeItemRequest = WriteItemRequest()
        every { dynamoClient.getDeleteRequest(exampleKey, AttributeExists("string")) } returns writeItemRequest
        underTest.getDeleteItemRequest(obj, AttributeExists("string")) shouldBe writeItemRequest
    }

    @Test
    fun canGetItemDescription() {
        underTest.getItemDescription().key shouldBe "string"
        underTest.getItemDescription().attributes shouldContainExactlyInAnyOrder setOf("integer")
    }
}
