package com.nimbusframework.nimbusaws.clients.keyvalue

import com.nimbusframework.nimbusaws.clients.dynamo.DynamoClient
import com.nimbusframework.nimbusaws.clients.dynamo.DynamoHelper.strAttribute
import com.nimbusframework.nimbusaws.examples.keyvalue.KeyValueStoreNoTableName
import com.nimbusframework.nimbuscore.clients.store.ReadItemRequest
import com.nimbusframework.nimbuscore.clients.store.WriteItemRequest
import com.nimbusframework.nimbuscore.clients.store.conditions.Condition
import com.nimbusframework.nimbuscore.clients.store.conditions.function.AttributeExists
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import java.lang.reflect.Field
import kotlin.streams.toList

class KeyValueStoreClientDynamoTest : AnnotationSpec() {

    private lateinit var underTest: KeyValueStoreClientDynamo<String, KeyValueStoreNoTableName>
    private lateinit var dynamoClient: DynamoClient

    private val attributes: Map<String, Field>
    private val exampleObj = mutableMapOf(Pair("value", strAttribute("test")))
    private val exampleKey = mapOf(Pair("PrimaryKey", strAttribute("key")))
    private val obj =
        KeyValueStoreNoTableName("test")

    init {
        val attributesMutable: MutableMap<String, Field> = mutableMapOf()
        attributesMutable["value"] = KeyValueStoreNoTableName::class.java.getDeclaredField("value")
        attributes = attributesMutable
    }

    @BeforeEach
    fun setUp() {
        dynamoClient = mockk(relaxed = true)
        every { dynamoClient.toAttributeValue("key") } returns strAttribute("key")

        underTest = KeyValueStoreClientDynamo(String::class.java, KeyValueStoreNoTableName::class.java, "dev", "PrimaryKey", "tableName", String::class.java) { dynamoClient }
    }

    @Test
    fun canPutItem() {
        underTest.put("key", obj)

        verify { dynamoClient.put(obj, attributes, exampleKey) }
    }

    @Test
    fun canPutItemWithCondition() {
        underTest.put("key", obj, AttributeExists("string"))

        verify { dynamoClient.put(obj, attributes, exampleKey, AttributeExists("string")) }
    }

    @Test
    fun canDeleteObj() {
        underTest.delete("key")

        verify { dynamoClient.deleteKey(exampleKey) }
    }

    @Test
    fun canDeleteObjWithCondition() {
        underTest.delete("key", AttributeExists("string"))

        verify { dynamoClient.deleteKey(exampleKey, AttributeExists("string")) }
    }

    @Test
    fun canGetAll() {
        val merged = exampleObj.toMutableMap();
        merged["PrimaryKey"] = strAttribute("key")

        every { dynamoClient.getAll() } returns listOf(merged.toMap()).stream()

        underTest.getAll()["key"] shouldBe obj
    }

    @Test
    fun canGetAllKeys() {
        val merged = mutableMapOf<String, AttributeValue>();
        merged["PrimaryKey"] = strAttribute("key")

        every { dynamoClient.getAllKeys() } returns listOf(merged.toMap()).stream()

        underTest.getAllKeys().toList() shouldContainExactly listOf("key")
    }

    @Test
    fun canFilter() {
        val merged = exampleObj.toMutableMap();
        merged["PrimaryKey"] = strAttribute("key")

        val condition = mockk<Condition>()
        every { dynamoClient.filter(condition) } returns listOf(merged.toMap()).stream()

        underTest.filter(condition)["key"] shouldBe obj
    }

    @Test
    fun canGetItem() {
        every { dynamoClient.get(exampleKey) } returns exampleObj

        underTest.get("key") shouldBe obj
    }

    @Test
    fun canGetReadItem() {
        val readItemRequest = ReadItemRequest<KeyValueStoreNoTableName>()
        every { dynamoClient.getReadItem<KeyValueStoreNoTableName>(exampleKey, captureLambda()) } returns readItemRequest

        underTest.getReadItem("key") shouldBe readItemRequest
    }

    @Test
    fun canGetWriteItem() {
        val writeItemRequest = WriteItemRequest()
        every { dynamoClient.getWriteItem(obj, attributes, exampleKey) } returns writeItemRequest

        underTest.getWriteItem("key", obj) shouldBe writeItemRequest
    }

    @Test
    fun canGetWriteItemWithCondition() {
        val writeItemRequest = WriteItemRequest()
        every { dynamoClient.getWriteItem(obj, attributes, exampleKey, AttributeExists("string")) } returns writeItemRequest

        underTest.getWriteItem("key", obj, AttributeExists("string")) shouldBe writeItemRequest
    }

    @Test
    fun canGetIncrementUpdateRequest() {
        val writeItemRequest = WriteItemRequest()
        every { dynamoClient.getUpdateValueRequest(exampleKey, "integer", 10, "+") } returns writeItemRequest
        underTest.getIncrementValueRequest("key", "integer", 10) shouldBe writeItemRequest
    }

    @Test
    fun canGetIncrementUpdateRequestWithCondition() {
        val writeItemRequest = WriteItemRequest()
        every { dynamoClient.getUpdateValueRequest(exampleKey, "integer", 10, "+", AttributeExists("string")) } returns writeItemRequest
        underTest.getIncrementValueRequest("key", "integer", 10, AttributeExists("string")) shouldBe writeItemRequest
    }

    @Test
    fun canGetDecrementUpdateRequest() {
        val writeItemRequest = WriteItemRequest()
        every { dynamoClient.getUpdateValueRequest(exampleKey, "integer", 10, "-") } returns writeItemRequest
        underTest.getDecrementValueRequest("key", "integer", 10) shouldBe writeItemRequest
    }

    @Test
    fun canGetDecrementUpdateRequestWithCondition() {
        val writeItemRequest = WriteItemRequest()
        every { dynamoClient.getUpdateValueRequest(exampleKey, "integer", 10, "-", AttributeExists("string")) } returns writeItemRequest
        underTest.getDecrementValueRequest("key", "integer", 10, AttributeExists("string")) shouldBe writeItemRequest
    }

    @Test
    fun canGetDeleteObjItemRequest() {
        val writeItemRequest = WriteItemRequest()
        every { dynamoClient.getDeleteRequest(exampleKey) } returns writeItemRequest
        underTest.getDeleteItemRequest("key") shouldBe writeItemRequest
    }

    @Test
    fun canGetDeleteObjItemRequestWithCondition() {
        val writeItemRequest = WriteItemRequest()
        every { dynamoClient.getDeleteRequest(exampleKey, AttributeExists("string")) } returns writeItemRequest
        underTest.getDeleteItemRequest("key", AttributeExists("string")) shouldBe writeItemRequest
    }

    @Test
    fun canGetItemDescription() {
        underTest.getItemDescription().key shouldBe "PrimaryKey"
        underTest.getItemDescription().attributes shouldContainExactlyInAnyOrder setOf("value")
    }

}
