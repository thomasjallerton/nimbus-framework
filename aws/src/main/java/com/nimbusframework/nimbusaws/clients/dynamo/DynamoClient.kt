package com.nimbusframework.nimbusaws.clients.dynamo

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.model.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusframework.nimbuscore.clients.store.ReadItemRequest
import com.nimbusframework.nimbuscore.clients.store.WriteItemRequest
import java.lang.reflect.Field

class DynamoClient<T>(private val tableName: String, private val clazz: Class<T>) {

    private val client = AmazonDynamoDBClientBuilder.defaultClient()
    private val objectMapper = ObjectMapper()

    fun put(obj: T, allAttributes: Map<String, Field>, additionalEntries:Map<String, AttributeValue> = mapOf()) {

        val attributeMap = getItem(obj, allAttributes, additionalEntries)
        val putItemRequest = PutItemRequest().withItem(attributeMap).withTableName(tableName)

        client.putItem(putItemRequest)
    }

    fun deleteKey(keyMap: Map<String, AttributeValue>) {

        val deleteItemRequest = DeleteItemRequest()
                .withKey(keyMap)
                .withTableName(tableName)

        client.deleteItem(deleteItemRequest)
    }

    fun getAll(): List<MutableMap<String, AttributeValue>> {
        val scanRequest = ScanRequest()
                .withTableName(tableName)
        val scanResult = client.scan(scanRequest)

        return scanResult.items
    }

    fun get(keyMap: Map<String, AttributeValue>, objectDef: Map<String, Field>): T? {

        val convertedMap = keyMap.mapValues { entry ->
            Condition().withComparisonOperator("EQ").withAttributeValueList(listOf(entry.value))
        }

        val queryRequest = QueryRequest()
                .withKeyConditions(convertedMap)
                .withTableName(tableName)

        val queryResult = client.query(queryRequest)

        return if (queryResult.count == 1) {
            toObject(queryResult.items[0], objectDef)
        } else {
            null
        }
    }

    fun getReadItem(keyMap: Map<String, AttributeValue>, objectDef: Map<String, Field>): ReadItemRequest<T> {
        val transactGetItem = TransactGetItem().withGet(Get().withKey(keyMap).withTableName(tableName))
        return DynamoReadItemRequest(transactGetItem) { item -> toObject(item.item, objectDef)}
    }

    fun getWriteItem(obj: T, allAttributes: Map<String, Field>, additionalEntries:Map<String, AttributeValue> = mapOf()): WriteItemRequest {
        val attributeMap = getItem(obj, allAttributes, additionalEntries)

        val transactWriteItem = TransactWriteItem().withPut(Put().withItem(attributeMap).withTableName(tableName))
        return DynamoWriteTransactItemRequest(transactWriteItem)
    }

    fun toAttributeValue(value: Any?): AttributeValue {
        return when (value) {
            is String -> AttributeValue(value)
            is Number -> AttributeValue().withN(value.toString())
            is Boolean -> AttributeValue().withBOOL(value)
            else -> AttributeValue().withS(objectMapper.writeValueAsString(value))
        }
    }

    fun <K> fromAttributeValue(value: AttributeValue, expectedType: Class<K>, fieldName: String): Any? {
        return when {
            value.bool != null && expectedType == Boolean::class.java -> value.bool
            value.n != null -> {
                when (expectedType) {
                    Integer::class.java -> value.n.toInt()
                    Double::class.java -> value.n.toDouble()
                    Long::class.java -> value.n.toLong()
                    Float::class.java -> value.n.toFloat()
                    else -> value.n.toInt()
                }
            }
            value.s != null && expectedType == String::class.java -> value.s
            value.s != null -> objectMapper.readValue(value.s, expectedType)
            else -> throw MismatchedTypeException(expectedType.simpleName, fieldName)
        }
    }

    fun toObject(obj: Map<String, AttributeValue>, objectDef: Map<String, Field>): T {

        val resultMap: MutableMap<String, Any?> = mutableMapOf()

        for ((columnName, field) in objectDef) {
            val attributeVal = obj[columnName]
            if (attributeVal != null) {
                resultMap[field.name] = fromAttributeValue(attributeVal, field.type, field.name)
            }
        }

        return objectMapper.convertValue(resultMap, clazz)
    }

    private fun getItem(obj: T, allAttributes: Map<String, Field>, additionalEntries:Map<String, AttributeValue> = mapOf()): Map<String, AttributeValue> {
        val attributeMap: MutableMap<String, AttributeValue> = mutableMapOf()

        for ((columnName, field) in allAttributes) {
            field.isAccessible = true
            attributeMap[columnName] = toAttributeValue(field.get(obj))
        }

        attributeMap.putAll(additionalEntries)
        return attributeMap;
    }
}