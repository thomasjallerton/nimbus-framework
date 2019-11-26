package com.nimbusframework.nimbusaws.clients.dynamo

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.model.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.Inject
import com.google.inject.assistedinject.Assisted
import com.nimbusframework.nimbusaws.clients.dynamo.condition.DynamoConditionProcessor
import com.nimbusframework.nimbusaws.wrappers.store.keyvalue.exceptions.ConditionFailedException
import com.nimbusframework.nimbuscore.clients.store.ReadItemRequest
import com.nimbusframework.nimbuscore.clients.store.WriteItemRequest
import com.nimbusframework.nimbuscore.clients.store.conditions.Condition
import com.nimbusframework.nimbuscore.exceptions.NonRetryableException
import com.nimbusframework.nimbuscore.exceptions.RetryableException
import java.lang.reflect.Field
import javax.naming.InvalidNameException

class DynamoClient @Inject constructor(
        @Assisted("tableName") private val tableName: String,
        @Assisted("className") private val className: String,
        @Assisted private val columnNameMap: Map<String, String>) {

    interface DynamoClientFactory {
        fun create(@Assisted("tableName") tableName: String,
                   @Assisted("className") className: String,
                   columnNameMap: Map<String, String>
        ): DynamoClient
    }

    @Inject
    private lateinit var client: AmazonDynamoDB

    private val conditionProcessor = DynamoConditionProcessor(this)
    private val objectMapper = ObjectMapper()

    fun put(obj: Any?, allAttributes: Map<String, Field>, additionalEntries:Map<String, AttributeValue> = mapOf(), condition: Condition? = null) {
        val attributeMap = getItem(obj, allAttributes, additionalEntries)
        val putItemRequest = PutItemRequest().withItem(attributeMap).withTableName(tableName)

        if (condition != null) {
            val valueMap: MutableMap<String, AttributeValue> = mutableMapOf()
            putItemRequest.withConditionExpression(conditionProcessor.processCondition(condition, valueMap))
                    .withExpressionAttributeValues(valueMap)
        }

        executeDynamoRequest { client.putItem(putItemRequest) }
    }

    fun deleteKey(keyMap: Map<String, AttributeValue>, condition: Condition? = null) {

        val deleteItemRequest = DeleteItemRequest()
                .withKey(keyMap)
                .withTableName(tableName)

        if (condition != null) {
            val valueMap: MutableMap<String, AttributeValue> = mutableMapOf()
            deleteItemRequest.withConditionExpression(conditionProcessor.processCondition(condition, valueMap))
                    .withExpressionAttributeValues(valueMap)
        }

        executeDynamoRequest { client.deleteItem(deleteItemRequest) }
    }

    fun getAll(): List<MutableMap<String, AttributeValue>> {
        val scanRequest = ScanRequest()
                .withTableName(tableName)
        return executeDynamoRequest { client.scan(scanRequest) }.items
    }

    fun get(keyMap: Map<String, AttributeValue>): MutableMap<String, AttributeValue>? {

        val convertedMap = keyMap.mapValues { entry ->
            Condition().withComparisonOperator("EQ").withAttributeValueList(listOf(entry.value))
        }

        val queryRequest = QueryRequest()
                .withKeyConditions(convertedMap)
                .withTableName(tableName)

        val queryResult = executeDynamoRequest { client.query(queryRequest) }

        return if (queryResult.count == 1) {
           queryResult.items[0]
        } else {
            null
        }
    }

    fun <T> getReadItem(keyMap: Map<String, AttributeValue>, transformer: (MutableMap<String, AttributeValue>) -> T): ReadItemRequest<T> {
        val transactGetItem = TransactGetItem().withGet(Get().withKey(keyMap).withTableName(tableName))
        return DynamoReadItemRequest(transactGetItem) { item -> transformer(item.item) }
    }

    fun getWriteItem(obj: Any?, allAttributes: Map<String, Field>, additionalEntries:Map<String, AttributeValue> = mapOf(), condition: Condition? = null): WriteItemRequest {
        val attributeMap = getItem(obj, allAttributes, additionalEntries)
        val put = Put().withItem(attributeMap).withTableName(tableName)

        if (condition != null) {
            val valueMap: MutableMap<String, AttributeValue> = mutableMapOf()
            put.withConditionExpression(conditionProcessor.processCondition(condition, valueMap))
                    .withExpressionAttributeValues(valueMap)
        }

        val transactWriteItem = TransactWriteItem().withPut(put)
        return DynamoWriteTransactItemRequest(transactWriteItem)
    }

    fun getUpdateValueRequest(keyMap: Map<String, AttributeValue>, fieldName: String, amount: Number, operator: String, condition: Condition? = null): WriteItemRequest {
        val columnName = getColumnName(fieldName)
        val attributeValues = mutableMapOf(Pair(":amount", toAttributeValue(amount)))
        val update = Update()
                .withKey(keyMap)
                .withUpdateExpression("set $columnName = $columnName $operator :amount")
                .withTableName(tableName)

        if (condition != null) {
            val conditionString = conditionProcessor.processCondition(condition, attributeValues)
            update.withConditionExpression(conditionString)
        }

        update.withExpressionAttributeValues(attributeValues)

        return DynamoWriteTransactItemRequest(TransactWriteItem().withUpdate(update))
    }

    fun getDeleteRequest(keyMap: Map<String, AttributeValue>, condition: Condition? = null): WriteItemRequest {
        val delete = Delete()
                .withKey(keyMap)
                .withTableName(tableName)

        if (condition != null) {
            val valueMap: MutableMap<String, AttributeValue> = mutableMapOf()
            val conditionString = conditionProcessor.processCondition(condition, valueMap)
            delete.withConditionExpression(conditionString)
                    .withExpressionAttributeValues(valueMap)
        }

        return DynamoWriteTransactItemRequest(TransactWriteItem().withDelete(delete))
    }

    fun toAttributeValue(value: Any?): AttributeValue {
        return when (value) {
            is String -> AttributeValue(value)
            is Number -> AttributeValue().withN(value.toString())
            is Boolean -> AttributeValue().withBOOL(value)
            else -> AttributeValue().withS(objectMapper.writeValueAsString(value))
        }
    }

    fun getColumnName(fieldName: String): String {
        return columnNameMap[fieldName] ?: throw InvalidNameException("$fieldName is not a field of $className")
    }

    private fun getItem(obj: Any?, allAttributes: Map<String, Field>, additionalEntries:Map<String, AttributeValue> = mapOf()): Map<String, AttributeValue> {
        val attributeMap: MutableMap<String, AttributeValue> = mutableMapOf()

        for ((columnName, field) in allAttributes) {
            field.isAccessible = true
            attributeMap[columnName] = toAttributeValue(field.get(obj))
        }

        attributeMap.putAll(additionalEntries)
        return attributeMap;
    }

    private fun <R> executeDynamoRequest(toExecute: () -> R): R {
        try {
            return toExecute()
        } catch (e: ConditionalCheckFailedException) {
            throw ConditionFailedException()
        } catch (e: AmazonDynamoDBException) {
            if (e.isRetryable) {
                throw RetryableException(e.localizedMessage)
            } else {
                e.printStackTrace()
                throw NonRetryableException(e.localizedMessage)
            }
        }
    }

}