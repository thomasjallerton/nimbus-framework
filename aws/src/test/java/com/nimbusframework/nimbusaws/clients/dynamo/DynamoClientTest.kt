package com.nimbusframework.nimbusaws.clients.dynamo

import com.amazonaws.AbortedException
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.model.*
import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.nimbusframework.nimbusaws.examples.DocumentStoreNoTableName
import com.nimbusframework.nimbusaws.wrappers.store.keyvalue.exceptions.ConditionFailedException
import com.nimbusframework.nimbuscore.annotations.persistent.Attribute
import com.nimbusframework.nimbuscore.annotations.persistent.Key
import com.nimbusframework.nimbuscore.clients.store.conditions.function.AttributeExists
import com.nimbusframework.nimbuscore.clients.store.conditions.function.AttributeNotExists
import com.nimbusframework.nimbuscore.exceptions.NonRetryableException
import com.nimbusframework.nimbuscore.exceptions.RetryableException
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.AnnotationSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import java.lang.reflect.Field
import javax.naming.InvalidNameException

class DynamoClientTest : AnnotationSpec() {

    private lateinit var underTest: DynamoClient
    private lateinit var mockDynamoDb: AmazonDynamoDB


    private val attributeMap: Map<String, AttributeValue>
    private val keyMap: Map<String, AttributeValue> = mapOf(Pair("string", AttributeValue("test")))

    private val attributes: MutableMap<String, Field> = mutableMapOf()
    private val columnNames: MutableMap<String, String> = mutableMapOf()
    private val obj = DocumentStoreNoTableName("test", 15)

    init {
        val numberVal = AttributeValue()
        numberVal.n = "15"
        attributeMap = mapOf(Pair("string", AttributeValue("test")), Pair("integer", numberVal))
        for (field in DocumentStoreNoTableName::class.java.declaredFields) {
            if (field.isAnnotationPresent(Attribute::class.java)) {
                val attributeAnnotation = field.getDeclaredAnnotation(Attribute::class.java)
                val columnName = if (attributeAnnotation.columnName != "") attributeAnnotation.columnName else field.name
                attributes[columnName] = field
                columnNames[field.name] = columnName
            } else if (field.isAnnotationPresent(Key::class.java)) {
                val attributeAnnotation = field.getAnnotation(Key::class.java)
                val columnName = if (attributeAnnotation.columnName != "") attributeAnnotation.columnName else field.name
                attributes[columnName] = field
                columnNames[field.name] = columnName
            }
        }
    }

    @BeforeEach
    fun setUp() {
        mockDynamoDb = mockk()
        val injector = Guice.createInjector(object: AbstractModule() {
            override fun configure() {
                bind(AmazonDynamoDB::class.java).toInstance(mockDynamoDb)
            }
        })
        underTest = DynamoClient("testTable", "test", columnNames)
        injector.injectMembers(underTest)
    }

    @Test
    fun canPutItemWithNoCondition() {
        val putItemRequest = slot<PutItemRequest>()

        every { mockDynamoDb.putItem(capture(putItemRequest)) } returns PutItemResult()

        underTest.put(obj, attributes)

        putItemRequest.captured.item shouldBe attributeMap
        putItemRequest.captured.tableName shouldBe "testTable"
        putItemRequest.captured.conditionExpression shouldBe null
    }

    @Test
    fun canPutItemWithCondition() {
        val putItemRequest = slot<PutItemRequest>()

        every { mockDynamoDb.putItem(capture(putItemRequest)) } returns PutItemResult()

        underTest.put(obj, attributes, mapOf(), AttributeNotExists("string"))

        putItemRequest.captured.item shouldBe attributeMap
        putItemRequest.captured.tableName shouldBe "testTable"
        putItemRequest.captured.conditionExpression shouldNotBe null
    }

    @Test
    fun canDeleteKeyWithNoCondition() {
        val deleteItemRequest = slot<DeleteItemRequest>()

        every { mockDynamoDb.deleteItem(capture(deleteItemRequest)) } returns DeleteItemResult()

        underTest.deleteKey(keyMap)

        deleteItemRequest.captured.key shouldBe keyMap
        deleteItemRequest.captured.tableName shouldBe "testTable"
        deleteItemRequest.captured.conditionExpression shouldBe null
    }

    @Test
    fun canDeleteKeyWithCondition() {
        val deleteItemRequest = slot<DeleteItemRequest>()

        every { mockDynamoDb.deleteItem(capture(deleteItemRequest)) } returns DeleteItemResult()

        underTest.deleteKey(keyMap, AttributeExists("string"))

        deleteItemRequest.captured.key shouldBe keyMap
        deleteItemRequest.captured.tableName shouldBe "testTable"
        deleteItemRequest.captured.conditionExpression shouldNotBe null
    }

    @Test
    fun canGetAllItems() {
        val scanRequest = slot<ScanRequest>()

        every { mockDynamoDb.scan(capture(scanRequest)) } returns ScanResult().withItems(attributeMap)

        underTest.getAll().size shouldBe 1

        scanRequest.captured.tableName = "testTable"
    }

    @Test
    fun canGetItem() {
        val queryRequest = slot<QueryRequest>()

        every { mockDynamoDb.query(capture(queryRequest)) } returns QueryResult().withItems(attributeMap).withCount(1)

        underTest.get(keyMap) shouldBe attributeMap

        val convertedMap = keyMap.mapValues { entry ->
            Condition().withComparisonOperator("EQ").withAttributeValueList(listOf(entry.value))
        }

        queryRequest.captured.tableName shouldBe "testTable"
        queryRequest.captured.keyConditions shouldBe convertedMap
    }

    @Test
    fun canGetReadItem() {
        val readItem = underTest.getReadItem(keyMap) {obj} as DynamoReadItemRequest<DocumentStoreNoTableName>
        readItem.transactReadItem.get.key shouldBe keyMap
        readItem.transactReadItem.get.tableName shouldBe "testTable"
        readItem.getItem(ItemResponse().withItem(attributeMap)) shouldBe obj
    }

    @Test
    fun canGetWriteItemRequestNoCondition() {
        val writeItem = underTest.getWriteItem(obj, attributes) as DynamoWriteTransactItemRequest

        writeItem.transactWriteItem.put.item shouldBe attributeMap
        writeItem.transactWriteItem.put.tableName shouldBe "testTable"
        writeItem.transactWriteItem.put.conditionExpression shouldBe null
    }

    @Test
    fun canGetWriteItemRequestWithCondition() {
        val writeItem = underTest.getWriteItem(obj, attributes, mapOf(), AttributeNotExists("string")) as DynamoWriteTransactItemRequest

        writeItem.transactWriteItem.put.item shouldBe attributeMap
        writeItem.transactWriteItem.put.tableName shouldBe "testTable"
        writeItem.transactWriteItem.put.conditionExpression shouldNotBe null
    }

    @Test
    fun canGetUpdateItemRequestNoCondition() {
        val updateItem = underTest.getUpdateValueRequest(keyMap, "integer", 10, "+") as DynamoWriteTransactItemRequest
        val numberVal = AttributeValue()
        numberVal.n = "10"

        updateItem.transactWriteItem.update.key shouldBe keyMap
        updateItem.transactWriteItem.update.tableName shouldBe "testTable"
        updateItem.transactWriteItem.update.updateExpression shouldBe "set integer = integer + :amount"
        updateItem.transactWriteItem.update.expressionAttributeValues shouldBe mapOf(Pair(":amount", numberVal))
        updateItem.transactWriteItem.update.conditionExpression shouldBe null
    }

    @Test
    fun canGetUpdateItemRequestWithCondition() {
        val updateItem = underTest.getUpdateValueRequest(keyMap, "integer", 10, "+", AttributeExists("string")) as DynamoWriteTransactItemRequest
        val numberVal = AttributeValue()
        numberVal.n = "10"

        updateItem.transactWriteItem.update.key shouldBe keyMap
        updateItem.transactWriteItem.update.tableName shouldBe "testTable"
        updateItem.transactWriteItem.update.updateExpression shouldBe "set integer = integer + :amount"
        updateItem.transactWriteItem.update.expressionAttributeValues shouldBe mapOf(Pair(":amount", numberVal))
        updateItem.transactWriteItem.update.conditionExpression shouldNotBe null
    }

    @Test
    fun canGetDeleteItemRequestNoCondition() {
        val writeItem = underTest.getDeleteRequest(keyMap) as DynamoWriteTransactItemRequest

        writeItem.transactWriteItem.delete.key shouldBe keyMap
        writeItem.transactWriteItem.delete.tableName shouldBe "testTable"
        writeItem.transactWriteItem.delete.conditionExpression shouldBe null
    }

    @Test
    fun canGetDeleteItemRequestWithCondition() {
        val writeItem = underTest.getDeleteRequest(keyMap, AttributeExists("string")) as DynamoWriteTransactItemRequest

        writeItem.transactWriteItem.delete.key shouldBe keyMap
        writeItem.transactWriteItem.delete.tableName shouldBe "testTable"
        writeItem.transactWriteItem.delete.conditionExpression shouldNotBe null
    }

    @Test
    fun canConvertToAttributeValue() {
        underTest.toAttributeValue(10).n shouldBe "10"
        underTest.toAttributeValue("10").s shouldBe "10"
        underTest.toAttributeValue(true).bool shouldBe true
    }

    @Test
    fun canGetColumnNameThatExists() {
        underTest.getColumnName("integer") shouldBe "integer"
    }

    @Test(expected = InvalidNameException::class)
    fun cannotGetColumnNameThatDoesNotExist() {
        underTest.getColumnName("test")
    }

    @Test(expected = ConditionFailedException::class)
    fun canExecuteDynamoRequestThrowsConditionException() {
        val putItemRequest = slot<PutItemRequest>()

        every { mockDynamoDb.putItem(capture(putItemRequest)) } throws ConditionalCheckFailedException("")

        underTest.put(obj, attributes)
    }

    @Test(expected = RetryableException::class)
    fun canExecuteDynamoRequestThrowsRetryableException() {
        val putItemRequest = slot<PutItemRequest>()

        every { mockDynamoDb.putItem(capture(putItemRequest)) } throws AmazonDynamoDBException("")

        underTest.put(obj, attributes)
    }

    @Test(expected = NonRetryableException::class)
    fun canExecuteDynamoRequestThrowsNonRetryableException() {
        val putItemRequest = slot<PutItemRequest>()

        every { mockDynamoDb.putItem(capture(putItemRequest)) } throws AbortedException("")

        underTest.put(obj, attributes)
    }

}