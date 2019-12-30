package com.nimbusframework.nimbusaws.clients.keyvalue

import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.nimbusframework.nimbusaws.clients.document.DocumentStoreClientDynamo
import com.nimbusframework.nimbusaws.clients.dynamo.DynamoClient
import com.nimbusframework.nimbusaws.examples.DocumentStoreNoTableName
import com.nimbusframework.nimbusaws.examples.KeyValueStoreNoTableName
import com.nimbusframework.nimbuscore.clients.store.ReadItemRequest
import com.nimbusframework.nimbuscore.clients.store.WriteItemRequest
import com.nimbusframework.nimbuscore.clients.store.conditions.function.AttributeExists
import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.specs.AnnotationSpec
import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.lang.reflect.Field

class KeyValueStoreClientDynamoTest : AnnotationSpec() {

    private lateinit var underTest: KeyValueStoreClientDynamo<String, KeyValueStoreNoTableName>
    private lateinit var dynamoClient: DynamoClient

    private val attributes: Map<String, Field>
    private val exampleObj = mutableMapOf(Pair("value", AttributeValue("test")))
    private val exampleKey = mapOf(Pair("PrimaryKey", AttributeValue("key")))
    private val obj = KeyValueStoreNoTableName("test")

    init {
        val attributesMutable: MutableMap<String, Field> = mutableMapOf()
        attributesMutable["value"] = KeyValueStoreNoTableName::class.java.getDeclaredField("value")
        attributes = attributesMutable
    }

    @BeforeEach
    fun setUp() {
        val dynamoFactory: DynamoClient.DynamoClientFactory = mockk()
        dynamoClient = mockk(relaxed = true)
        every { dynamoClient.toAttributeValue("key") } returns AttributeValue("key")

        every { dynamoFactory.create("tableName", "com.nimbusframework.nimbusaws.examples.KeyValueStoreNoTableName", any()) } returns dynamoClient

        underTest = KeyValueStoreClientDynamo(String::class.java, KeyValueStoreNoTableName::class.java, "dev", "PrimaryKey", "tableName", String::class.java)
        val injector = Guice.createInjector(object: AbstractModule() {
            override fun configure() {
                bind(DynamoClient.DynamoClientFactory::class.java).toInstance(dynamoFactory)
            }
        })
        injector.injectMembers(underTest)
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
        merged["PrimaryKey"] = AttributeValue("key")

        every { dynamoClient.getAll() } returns listOf(merged)

        underTest.getAll()["key"] shouldBe obj
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