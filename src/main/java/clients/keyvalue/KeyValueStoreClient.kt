package clients.keyvalue

import annotation.annotations.keyvalue.Attribute
import annotation.annotations.keyvalue.Key
import annotation.annotations.keyvalue.KeyValueStore
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.model.*
import com.fasterxml.jackson.databind.ObjectMapper
import wrappers.keyvalue.exceptions.ConditionFailedException
import java.lang.reflect.Field

class KeyValueStoreClient<T>(val clazz: Class<T>) {

    private val client = AmazonDynamoDBClientBuilder.defaultClient()

    private val keys: MutableList<Field> = mutableListOf()
    private val allAttributes: MutableList<Field> = mutableListOf()
    private val tableName: String

    init {
        val keyValueStoreAnnotation = clazz.getDeclaredAnnotation(KeyValueStore::class.java)
        tableName = if (keyValueStoreAnnotation.tableName != "") keyValueStoreAnnotation.tableName else clazz.simpleName


        for (field in clazz.declaredFields) {
            if (field.isAnnotationPresent(Key::class.java)) {
                keys.add(field)
                allAttributes.add(field)
            } else if (field.isAnnotationPresent(Attribute::class.java)) {
                allAttributes.add(field)
            }
        }
    }


    fun saveItem(obj: T) {
        val attributeMap: MutableMap<String, AttributeValue> = mutableMapOf()

        for (attribute in allAttributes) {
            attribute.isAccessible = true
            attributeMap[attribute.name] = toAttributeValue(attribute.get(obj))
        }

        val putItemRequest = PutItemRequest().withItem(attributeMap).withTableName(tableName)

        client.putItem(putItemRequest)
    }

    fun deleteItem(obj: T) {

        val deleteItemRequest = DeleteItemRequest()
                .withKey(getKeys(obj))
                .withTableName(tableName)


        client.deleteItem(deleteItemRequest)
    }

    @Throws(ConditionFailedException::class)
    fun deleteItem(keyObj: T, conditionExpression: String, expressionValues: Map<String, Any>) {

        val convertedValues = expressionValues.mapValues { entry -> toAttributeValue(entry.value) }

        try {
            val deleteItemRequest = DeleteItemRequest()
                    .withKey(getKeys(keyObj))
                    .withConditionExpression(conditionExpression)
                    .withExpressionAttributeValues(convertedValues)
                    .withTableName(tableName)
            client.deleteItem(deleteItemRequest)
        } catch (e: ConditionalCheckFailedException) {
            throw ConditionFailedException()
        }
    }

    fun scan(): List<T> {
        val scanRequest = ScanRequest()
                .withTableName(tableName)
        val scanResult = client.scan(scanRequest)

        return scanResult.items.map { valueMap -> toObject(valueMap) }
    }

    fun scan(conditionExpression: String, expressionValues: Map<String, Any>): List<T> {

        val convertedValues = expressionValues.mapValues { entry -> toAttributeValue(entry.value) }

        val scanRequest = ScanRequest()
                .withFilterExpression(conditionExpression)
                .withExpressionAttributeValues(convertedValues)
                .withTableName(tableName)
        val scanResult = client.scan(scanRequest)

        return scanResult.items.map { valueMap -> toObject(valueMap) }
    }

    fun query(keyMap: Map<String, Any>): List<T> {

        val convertedMap = keyMap.mapValues { entry ->
            Condition().withComparisonOperator("EQ").withAttributeValueList(listOf(toAttributeValue(entry.value)))
        }


        val queryRequest = QueryRequest()
                .withKeyConditions(convertedMap)
                .withTableName(tableName)

        val queryResult = client.query(queryRequest)

        return queryResult.items.map { valueMap -> toObject(valueMap) }
    }

    private fun toAttributeValue(value: Any): AttributeValue {
        return when (value) {
            is String -> AttributeValue(value)
            is Number -> AttributeValue().withN(value.toString())
            is Boolean -> AttributeValue().withBOOL(value)
            else -> AttributeValue()
        }
    }

    private fun fromAttributeValue(value: AttributeValue): Any {
        return when {
            value.bool != null -> value.bool
            value.n != null -> value.n.toDouble()
            value.s != null -> value.s
            else -> Any()
        }
    }

    private fun getKeys(obj: T): Map<String, AttributeValue> {
        val keyMap: MutableMap<String, AttributeValue> = mutableMapOf()

        for (key in keys) {
            key.isAccessible = true
            keyMap[key.name] = toAttributeValue(key.get(obj))
        }
        return keyMap
    }

    private fun toObject(map: Map<String, AttributeValue>): T {
        val convertedMap = map.mapValues { entry -> fromAttributeValue(entry.value) }

        val mapper = ObjectMapper()
        return mapper.convertValue(convertedMap, clazz)
    }

}