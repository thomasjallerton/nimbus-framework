package clients.keyvalue

import annotation.annotations.keyvalue.KeyType
import annotation.annotations.keyvalue.KeyValueStore
import annotation.annotations.persistent.Attribute
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.model.*
import com.fasterxml.jackson.databind.ObjectMapper
import java.lang.reflect.Field

class KeyValueStoreClient<T>(private val clazz: Class<T>) {

    private val client = AmazonDynamoDBClientBuilder.defaultClient()

    private val keyType: KeyType
    private val keyName: String
    private val attributes: MutableList<Field> = mutableListOf()
    private val tableName: String

    init {
        val keyValueStoreAnnotation = clazz.getDeclaredAnnotation(KeyValueStore::class.java)
        tableName = if (keyValueStoreAnnotation.tableName != "") keyValueStoreAnnotation.tableName else clazz.simpleName
        keyType = keyValueStoreAnnotation.keyType
        keyName = keyValueStoreAnnotation.keyName

        for (field in clazz.declaredFields) {
            if (field.isAnnotationPresent(Attribute::class.java)) {
                attributes.add(field)
                if (field.name == keyName) throw AttributeNameException()
            }
        }
    }


    fun put(key: Any, value: T) {
        val attributeMap: MutableMap<String, AttributeValue> = mutableMapOf()

        for (attribute in attributes) {
            attribute.isAccessible = true
            attributeMap[attribute.name] = toAttributeValue(attribute.get(value))
        }

        attributeMap[keyName] = keyToAttributeValue(key)

        val putItemRequest = PutItemRequest().withItem(attributeMap).withTableName(tableName)

        client.putItem(putItemRequest)
    }

    fun delete(keyObj: Any) {

        val convertedValue = keyToAttributeValue(keyObj)

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

    fun get(keyObj: Any): T? {

        val keyMap = keyToKeyMap(keyObj)

        val convertedMap = keyMap.mapValues { entry ->
            Condition().withComparisonOperator("EQ").withAttributeValueList(listOf(entry.value))
        }

        val queryRequest = QueryRequest()
                .withKeyConditions(convertedMap)
                .withTableName(tableName)

        val queryResult = client.query(queryRequest)

        return if (queryResult.count == 1) {
            toObject(queryResult.items[0])
        } else {
            null
        }
    }

    private fun toAttributeValue(value: Any): AttributeValue {
        return when (value) {
            is String -> AttributeValue(value)
            is Number -> AttributeValue().withN(value.toString())
            is Boolean -> AttributeValue().withBOOL(value)
            else -> AttributeValue()
        }
    }

    private fun keyToAttributeValue(value: Any): AttributeValue {
        return when (value) {
            is String -> {
                if (keyType != KeyType.STRING) throw MismatchedKeyException(keyType.name)
                AttributeValue(value)
            }
            is Number -> {
                if (keyType != KeyType.NUMBER) throw MismatchedKeyException(keyType.name)
                AttributeValue().withN(value.toString())
            }
            is Boolean -> {
                if (keyType != KeyType.BOOLEAN) throw MismatchedKeyException(keyType.name)
                AttributeValue().withBOOL(value)
            }
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

    private fun keyToKeyMap(keyObj: Any): Map<String, AttributeValue> {
        val keyMap: MutableMap<String, AttributeValue> = mutableMapOf()

        keyMap[keyName] = keyToAttributeValue(keyObj)

        return keyMap
    }

    private fun toObject(map: MutableMap<String, AttributeValue>): T {
        map.remove(keyName)
        val convertedMap = map.mapValues { entry -> fromAttributeValue(entry.value) }

        val mapper = ObjectMapper()
        return mapper.convertValue(convertedMap, clazz)
    }
}