package clients.document

import annotation.annotations.document.Attribute
import annotation.annotations.document.Key
import annotation.annotations.document.DocumentStore
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.model.*
import com.fasterxml.jackson.databind.ObjectMapper
import java.lang.reflect.Field

class DocumentStoreClient<T>(private val clazz: Class<T>) {

    private val client = AmazonDynamoDBClientBuilder.defaultClient()

    private val keys: MutableList<Field> = mutableListOf()
    private val allAttributes: MutableList<Field> = mutableListOf()
    private val tableName: String

    init {
        val documentStoreAnnotation = clazz.getDeclaredAnnotation(DocumentStore::class.java)
        tableName = if (documentStoreAnnotation.tableName != "") documentStoreAnnotation.tableName else clazz.simpleName


        for (field in clazz.declaredFields) {
            if (field.isAnnotationPresent(Key::class.java)) {
                keys.add(field)
                allAttributes.add(field)
            } else if (field.isAnnotationPresent(Attribute::class.java)) {
                allAttributes.add(field)
            }
        }
    }


    fun put(obj: T) {
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
                .withKey(objectToKeyMap(obj))
                .withTableName(tableName)

        client.deleteItem(deleteItemRequest)
    }

    fun deleteKey(keyObj: Any) {

        val convertedValue = toAttributeValue(keyObj)

        val deleteItemRequest = DeleteItemRequest()
                .withKey(keyToKeyMap(keyObj))
                .withTableName(tableName)

        client.deleteItem(deleteItemRequest)
    }

    fun getAll(): List<T> {
        val scanRequest = ScanRequest()
                .withTableName(tableName)
        val scanResult = client.scan(scanRequest)

        return scanResult.items.map { valueMap -> toObject(valueMap) }
    }

    fun get(keyObj: Any): List<T> {

        val keyMap = keyToKeyMap(keyObj)

        val convertedMap = keyMap.mapValues { entry ->
            Condition().withComparisonOperator("EQ").withAttributeValueList(listOf(entry.value))
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

    private fun objectToKeyMap(obj: T): Map<String, AttributeValue> {
        val keyMap: MutableMap<String, AttributeValue> = mutableMapOf()

        for (key in keys) {
            key.isAccessible = true
            keyMap[key.name] = toAttributeValue(key.get(obj))
        }
        return keyMap
    }

    private fun keyToKeyMap(keyObj: Any): Map<String, AttributeValue> {
        if (keys.size > 1) {
            throw Exception("Composite key shouldn't exist!!")
        }
        val keyMap: MutableMap<String, AttributeValue> = mutableMapOf()

        for (key in keys) {
            keyMap[key.name] = toAttributeValue(keyObj)
        }
        return keyMap
    }

    private fun toObject(map: Map<String, AttributeValue>): T {
        val convertedMap = map.mapValues { entry -> fromAttributeValue(entry.value) }

        val mapper = ObjectMapper()
        return mapper.convertValue(convertedMap, clazz)
    }

}