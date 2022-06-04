package com.nimbusframework.nimbusaws.clients.dynamo

import com.nimbusframework.nimbusaws.clients.dynamo.condition.DynamoConditionProcessor
import com.nimbusframework.nimbusaws.examples.document.DocumentStoreNoTableName
import com.nimbusframework.nimbusaws.wrappers.store.keyvalue.exceptions.ConditionFailedException
import com.nimbusframework.nimbuscore.annotations.persistent.Attribute
import com.nimbusframework.nimbuscore.annotations.persistent.Key
import com.nimbusframework.nimbuscore.clients.store.conditions.ComparisonCondition
import com.nimbusframework.nimbuscore.clients.store.conditions.ComparisonOperator
import com.nimbusframework.nimbuscore.clients.store.conditions.bool.BooleanComparisonCondition
import com.nimbusframework.nimbuscore.clients.store.conditions.function.AttributeExists
import com.nimbusframework.nimbuscore.clients.store.conditions.function.AttributeNotExists
import com.nimbusframework.nimbuscore.clients.store.conditions.variable.ConditionVariable
import com.nimbusframework.nimbuscore.exceptions.NonRetryableException
import com.nimbusframework.nimbuscore.exceptions.RetryableException
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import software.amazon.awssdk.core.exception.AbortedException
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.*
import java.lang.reflect.Field
import javax.naming.InvalidNameException
import kotlin.streams.toList

class DynamoClientTest : AnnotationSpec() {

    private lateinit var underTest: DynamoClient
    private lateinit var mockDynamoDb: DynamoDbClient


    private val attributeMap: Map<String, AttributeValue>
    private val keyMap: Map<String, AttributeValue> = mapOf(Pair("string", AttributeValue.builder().s("test").build()))

    private val attributes: MutableMap<String, Field> = mutableMapOf()
    private val columnNames: MutableMap<String, String> = mutableMapOf()
    private val obj = DocumentStoreNoTableName("test", 15)

    init {
        val numberVal = AttributeValue.builder()
            .n("15")
            .build()
        attributeMap = mapOf(Pair("string", AttributeValue.builder().s("test").build()), Pair("integer", numberVal))
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
        underTest = DynamoClient("testTable", "test", "string", columnNames, mockDynamoDb)
    }

    @Test
    fun canPutItemWithNoCondition() {
        val putItemRequest = slot<PutItemRequest>()

        every { mockDynamoDb.putItem(capture(putItemRequest)) } returns PutItemResponse.builder().build()

        underTest.put(obj, attributes)

        putItemRequest.captured.item() shouldBe attributeMap
        putItemRequest.captured.tableName() shouldBe "testTable"
        putItemRequest.captured.conditionExpression() shouldBe null
    }

    @Test
    fun canPutItemWithCondition() {
        val putItemRequest = slot<PutItemRequest>()

        every { mockDynamoDb.putItem(capture(putItemRequest)) } returns PutItemResponse.builder().build()

        underTest.put(obj, attributes, mapOf(), AttributeNotExists("string"))

        putItemRequest.captured.item() shouldBe attributeMap
        putItemRequest.captured.tableName() shouldBe "testTable"
        putItemRequest.captured.conditionExpression() shouldNotBe null
    }

    @Test
    fun canDeleteKeyWithNoCondition() {
        val deleteItemRequest = slot<DeleteItemRequest>()

        every { mockDynamoDb.deleteItem(capture(deleteItemRequest)) } returns DeleteItemResponse.builder().build()

        underTest.deleteKey(keyMap)

        deleteItemRequest.captured.key() shouldBe keyMap
        deleteItemRequest.captured.tableName() shouldBe "testTable"
        deleteItemRequest.captured.conditionExpression() shouldBe null
    }

    @Test
    fun canDeleteKeyWithCondition() {
        val deleteItemRequest = slot<DeleteItemRequest>()

        every { mockDynamoDb.deleteItem(capture(deleteItemRequest)) } returns DeleteItemResponse.builder().build()

        underTest.deleteKey(keyMap, AttributeExists("string"))

        deleteItemRequest.captured.key() shouldBe keyMap
        deleteItemRequest.captured.tableName() shouldBe "testTable"
        deleteItemRequest.captured.conditionExpression() shouldNotBe null
    }

    @Test
    fun canGetAllItems() {
        val scanRequest = slot<ScanRequest>()

        every { mockDynamoDb.scan(capture(scanRequest)) } returns ScanResponse.builder().items(attributeMap).build()

        underTest.getAll().count() shouldBe 1

        scanRequest.captured.tableName() shouldBe "testTable"
    }

    @Test
    fun canGetAllKeys() {
        val scanRequest = slot<ScanRequest>()

        every { mockDynamoDb.scan(capture(scanRequest)) } returns ScanResponse.builder().items(attributeMap.filter { it.key == "string" }).build()

        underTest.getAllKeys().toList() shouldContainExactly listOf(mapOf(Pair("string", AttributeValue.builder().s("test").build())))

        scanRequest.captured.tableName() shouldBe "testTable"
        scanRequest.captured.projectionExpression() shouldBe "string"
    }

    @Test
    fun canFilterItemsNoPagination() {
        val scanRequest = slot<ScanRequest>()

        every { mockDynamoDb.scan(capture(scanRequest)) } returns ScanResponse.builder()
            .items(attributeMap)
            .lastEvaluatedKey(null)
            .build()

        underTest.filter(AttributeNotExists("string")).count() shouldBe 1

        scanRequest.captured.tableName() shouldBe "testTable"
        scanRequest.captured.filterExpression() shouldBe "attribute_not_exists ( string )"
        scanRequest.captured.expressionAttributeValues() shouldBe mapOf()
    }

    @Test
    fun canFilterItemsPagination() {
        val scanRequest = mutableListOf<ScanRequest>()

        val condition = ComparisonCondition(ConditionVariable.column("string"), ComparisonOperator.EQUAL, ConditionVariable.string("test"))
        val attributeValues = mutableMapOf<String, AttributeValue>()
        val conditionStr = DynamoConditionProcessor(underTest).processCondition(
            condition,
            attributeValues
        )

        val item2 = mapOf(Pair("string", AttributeValue.builder().s("test2").build()), Pair("integer", AttributeValue.builder()
            .n("13")
            .build())
        )

        every { mockDynamoDb.scan(capture(scanRequest)) } returnsMany listOf(
            ScanResponse.builder()
                .items(attributeMap)
                .lastEvaluatedKey(mapOf(Pair("string", AttributeValue.builder().s("test").build())))
                .build(),
            ScanResponse.builder()
                .items(item2)
                .lastEvaluatedKey(null)
                .build()
        )

        underTest.filter(condition).toList() shouldContainExactlyInAnyOrder listOf(attributeMap, item2)

        scanRequest shouldHaveSize 2
        scanRequest[0].tableName() shouldBe "testTable"
        scanRequest[0].exclusiveStartKey() shouldBe mapOf()
        scanRequest[0].filterExpression() shouldBe conditionStr
        scanRequest[0].expressionAttributeValues() shouldBe attributeValues

        scanRequest[1].tableName() shouldBe "testTable"
        scanRequest[1].exclusiveStartKey() shouldBe mapOf(Pair("string", AttributeValue.builder().s("test").build()))
        scanRequest[1].filterExpression() shouldBe conditionStr
        scanRequest[1].expressionAttributeValues() shouldBe attributeValues
    }

    @Test
    fun canGetItem() {
        val queryRequest = slot<QueryRequest>()

        every { mockDynamoDb.query(capture(queryRequest)) } returns QueryResponse.builder().items(attributeMap).count(1).build()

        underTest.get(keyMap) shouldBe attributeMap

        val convertedMap = keyMap.mapValues { entry ->
            Condition.builder().comparisonOperator("EQ").attributeValueList(listOf(entry.value)).build()
        }

        queryRequest.captured.tableName() shouldBe "testTable"
        queryRequest.captured.keyConditions() shouldBe convertedMap
    }

    @Test
    fun canGetReadItem() {
        val readItem = underTest.getReadItem(keyMap) { obj } as DynamoReadItemRequest<DocumentStoreNoTableName>
        readItem.transactReadItem.get().key() shouldBe keyMap
        readItem.transactReadItem.get().tableName() shouldBe "testTable"
        readItem.getItem(ItemResponse.builder().item(attributeMap).build()) shouldBe obj
    }

    @Test
    fun canGetWriteItemRequestNoCondition() {
        val writeItem = underTest.getWriteItem(obj, attributes) as DynamoWriteTransactItemRequest

        writeItem.transactWriteItem.put().item() shouldBe attributeMap
        writeItem.transactWriteItem.put().tableName() shouldBe "testTable"
        writeItem.transactWriteItem.put().conditionExpression() shouldBe null
    }

    @Test
    fun canGetWriteItemRequestWithCondition() {
        val writeItem = underTest.getWriteItem(obj, attributes, mapOf(), AttributeNotExists("string")) as DynamoWriteTransactItemRequest

        writeItem.transactWriteItem.put().item() shouldBe attributeMap
        writeItem.transactWriteItem.put().tableName() shouldBe "testTable"
        writeItem.transactWriteItem.put().conditionExpression() shouldNotBe null
    }

    @Test
    fun canGetUpdateItemRequestNoCondition() {
        val updateItem = underTest.getUpdateValueRequest(keyMap, "integer", 10, "+") as DynamoWriteTransactItemRequest
        val numberVal = AttributeValue.builder().n("10").build()

        updateItem.transactWriteItem.update().key() shouldBe keyMap
        updateItem.transactWriteItem.update().tableName() shouldBe "testTable"
        updateItem.transactWriteItem.update().updateExpression() shouldBe "set integer = integer + :amount"
        updateItem.transactWriteItem.update().expressionAttributeValues() shouldBe mapOf(Pair(":amount", numberVal))
        updateItem.transactWriteItem.update().conditionExpression() shouldBe null
    }

    @Test
    fun canGetUpdateItemRequestWithCondition() {
        val updateItem = underTest.getUpdateValueRequest(keyMap, "integer", 10, "+", AttributeExists("string")) as DynamoWriteTransactItemRequest
        val numberVal = AttributeValue.builder().n("10").build()

        updateItem.transactWriteItem.update().key() shouldBe keyMap
        updateItem.transactWriteItem.update().tableName() shouldBe "testTable"
        updateItem.transactWriteItem.update().updateExpression() shouldBe "set integer = integer + :amount"
        updateItem.transactWriteItem.update().expressionAttributeValues() shouldBe mapOf(Pair(":amount", numberVal))
        updateItem.transactWriteItem.update().conditionExpression() shouldNotBe null
    }

    @Test
    fun canGetDeleteItemRequestNoCondition() {
        val writeItem = underTest.getDeleteRequest(keyMap) as DynamoWriteTransactItemRequest

        writeItem.transactWriteItem.delete().key() shouldBe keyMap
        writeItem.transactWriteItem.delete().tableName() shouldBe "testTable"
        writeItem.transactWriteItem.delete().conditionExpression() shouldBe null
    }

    @Test
    fun canGetDeleteItemRequestWithCondition() {
        val writeItem = underTest.getDeleteRequest(keyMap, AttributeExists("string")) as DynamoWriteTransactItemRequest

        writeItem.transactWriteItem.delete().key() shouldBe keyMap
        writeItem.transactWriteItem.delete().tableName() shouldBe "testTable"
        writeItem.transactWriteItem.delete().conditionExpression() shouldNotBe null
    }

    @Test
    fun canConvertToAttributeValue() {
        underTest.toAttributeValue(10).n() shouldBe "10"
        underTest.toAttributeValue("10").s() shouldBe "10"
        underTest.toAttributeValue(true).bool() shouldBe true
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

        every { mockDynamoDb.putItem(capture(putItemRequest)) } throws ConditionalCheckFailedException.builder().build()

        underTest.put(obj, attributes)
    }

    @Test(expected = RetryableException::class)
    fun canExecuteDynamoRequestThrowsRetryableException() {
        val putItemRequest = slot<PutItemRequest>()

        every { mockDynamoDb.putItem(capture(putItemRequest)) } throws software.amazon.awssdk.core.exception.RetryableException.builder().message("hello").build()

        underTest.put(obj, attributes)
    }

    @Test(expected = NonRetryableException::class)
    fun canExecuteDynamoRequestThrowsNonRetryableException() {
        val putItemRequest = slot<PutItemRequest>()

        every { mockDynamoDb.putItem(capture(putItemRequest)) } throws AbortedException.builder().message("hello").build()

        underTest.put(obj, attributes)
    }

}
