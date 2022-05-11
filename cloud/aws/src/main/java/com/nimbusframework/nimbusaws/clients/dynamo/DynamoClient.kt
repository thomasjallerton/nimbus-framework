package com.nimbusframework.nimbusaws.clients.dynamo

import com.nimbusframework.nimbusaws.clients.dynamo.condition.DynamoConditionProcessor
import com.nimbusframework.nimbusaws.wrappers.store.keyvalue.exceptions.ConditionFailedException
import com.nimbusframework.nimbuscore.clients.JacksonClient
import com.nimbusframework.nimbuscore.clients.store.ReadItemRequest
import com.nimbusframework.nimbuscore.clients.store.WriteItemRequest
import com.nimbusframework.nimbuscore.clients.store.conditions.Condition
import com.nimbusframework.nimbuscore.exceptions.NonRetryableException
import com.nimbusframework.nimbuscore.exceptions.RetryableException
import software.amazon.awssdk.core.exception.SdkException
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.*
import java.lang.reflect.Field
import java.util.stream.Stream
import javax.naming.InvalidNameException

class DynamoClient (
        private val tableName: String,
        private val className: String,
        private val columnNameMap: Map<String, String>,
        private val client: DynamoDbClient
) {

    private val conditionProcessor = DynamoConditionProcessor(this)

    fun put(obj: Any?, allAttributes: Map<String, Field>, additionalEntries:Map<String, AttributeValue> = mapOf(), condition: Condition? = null) {
        val attributeMap = getItem(obj, allAttributes, additionalEntries)
        val putItemRequest = PutItemRequest.builder()
            .item(attributeMap)
            .tableName(tableName)

        if (condition != null) {
            val valueMap: MutableMap<String, AttributeValue> = mutableMapOf()
            putItemRequest.conditionExpression(conditionProcessor.processCondition(condition, valueMap))
                    .expressionAttributeValues(valueMap)
        }

        executeDynamoRequest { client.putItem(putItemRequest.build()) }
    }

    fun deleteKey(keyMap: Map<String, AttributeValue>, condition: Condition? = null) {

        val deleteItemRequest = DeleteItemRequest.builder()
                .key(keyMap)
                .tableName(tableName)

        if (condition != null) {
            val valueMap: MutableMap<String, AttributeValue> = mutableMapOf()
            deleteItemRequest
                .conditionExpression(conditionProcessor.processCondition(condition, valueMap))
                .expressionAttributeValues(valueMap)
        }

        executeDynamoRequest { client.deleteItem(deleteItemRequest.build()) }
    }

    fun getAll(): List<MutableMap<String, AttributeValue>> {
        val scanRequest = ScanRequest.builder()
                .tableName(tableName)
        return executeDynamoRequest { client.scan(scanRequest.build()) }.items()
    }

    fun get(keyMap: Map<String, AttributeValue>): MutableMap<String, AttributeValue>? {

        val convertedMap = keyMap.mapValues { entry ->
            software.amazon.awssdk.services.dynamodb.model.Condition.builder().comparisonOperator("EQ").attributeValueList(listOf(entry.value)).build()
        }

        val queryRequest = QueryRequest.builder()
                .keyConditions(convertedMap)
                .tableName(tableName)

        val queryResult = executeDynamoRequest { client.query(queryRequest.build()) }

        return if (queryResult.count() == 1) {
           queryResult.items()[0]
        } else {
            null
        }
    }

    fun filter(attributeCondition: Condition): List<Map<String, AttributeValue>> {
        val expressionValues = mutableMapOf<String, AttributeValue>()
        val filterExpression = conditionProcessor.processCondition(attributeCondition, expressionValues)
        val scanRequest = ScanRequest.builder()
            .tableName(tableName)
            .filterExpression(filterExpression)
            .expressionAttributeValues(expressionValues)

        val result = mutableListOf<Map<String, AttributeValue>>()

        var hasLastEvaluatedKey = true
        while (hasLastEvaluatedKey) {
            val scanResponse = executeDynamoRequest { client.scan(scanRequest.build()) }

            hasLastEvaluatedKey = scanResponse.hasLastEvaluatedKey()
            if (hasLastEvaluatedKey) {
                scanRequest.exclusiveStartKey(scanResponse.lastEvaluatedKey())
            }

            result.addAll(scanResponse.items())
        }
        return result
    }

    fun <T> getReadItem(keyMap: Map<String, AttributeValue>, transformer: (MutableMap<String, AttributeValue>) -> T): ReadItemRequest<T> {
        val transactGetItem = TransactGetItem.builder().get(Get.builder().key(keyMap).tableName(tableName).build()).build()
        return DynamoReadItemRequest(transactGetItem) { item -> transformer(item.item()) }
    }

    fun getWriteItem(obj: Any?, allAttributes: Map<String, Field>, additionalEntries:Map<String, AttributeValue> = mapOf(), condition: Condition? = null): WriteItemRequest {
        val attributeMap = getItem(obj, allAttributes, additionalEntries)
        val put = Put.builder().item(attributeMap).tableName(tableName)

        if (condition != null) {
            val valueMap: MutableMap<String, AttributeValue> = mutableMapOf()
            put.conditionExpression(conditionProcessor.processCondition(condition, valueMap))
                    .expressionAttributeValues(valueMap)
        }

        val transactWriteItem = TransactWriteItem.builder().put(put.build()).build()
        return DynamoWriteTransactItemRequest(transactWriteItem)
    }

    fun getUpdateValueRequest(keyMap: Map<String, AttributeValue>, fieldName: String, amount: Number, operator: String, condition: Condition? = null): WriteItemRequest {
        val columnName = getColumnName(fieldName)
        val attributeValues = mutableMapOf(Pair(":amount", toAttributeValue(amount)))
        val update = Update.builder()
                .key(keyMap)
                .updateExpression("set $columnName = $columnName $operator :amount")
                .tableName(tableName)

        if (condition != null) {
            val conditionString = conditionProcessor.processCondition(condition, attributeValues)
            update.conditionExpression(conditionString)
        }

        update.expressionAttributeValues(attributeValues)

        return DynamoWriteTransactItemRequest(TransactWriteItem.builder().update(update.build()).build())
    }

    fun getDeleteRequest(keyMap: Map<String, AttributeValue>, condition: Condition? = null): WriteItemRequest {
        val delete = Delete.builder()
                .key(keyMap)
                .tableName(tableName)

        if (condition != null) {
            val valueMap: MutableMap<String, AttributeValue> = mutableMapOf()
            val conditionString = conditionProcessor.processCondition(condition, valueMap)
            delete.conditionExpression(conditionString)
                    .expressionAttributeValues(valueMap)
        }

        return DynamoWriteTransactItemRequest(TransactWriteItem.builder().delete(delete.build()).build())
    }

    fun toAttributeValue(value: Any?): AttributeValue {
        val builder = AttributeValue.builder()
        return when (value) {
            is String -> builder.s(value)
            is Number -> builder.n(value.toString())
            is Boolean -> builder.bool(value)
            else -> builder.s(JacksonClient.writeValueAsString(value))
        }.build()
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
        return attributeMap
    }

    private fun <R> executeDynamoRequest(toExecute: () -> R): R {
        try {
            return toExecute()
        } catch (e: ConditionalCheckFailedException) {
            throw ConditionFailedException()
        } catch (e: SdkException) {
            if (e.retryable()) {
                throw RetryableException(e.localizedMessage)
            } else {
                e.printStackTrace()
                throw NonRetryableException(e.localizedMessage)
            }
        }
    }

}
