package clients.keyvalue

import annotation.annotations.keyvalue.KeyType
import annotation.annotations.keyvalue.KeyValueStore
import annotation.annotations.persistent.Attribute
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.model.*
import com.fasterxml.jackson.databind.ObjectMapper
import java.lang.reflect.Field
import kotlin.reflect.full.superclasses

class KeyValueStoreClient<K, V>(private val keyClass: Class<K> , private val valueClass: Class<V>) {

    private val client = AmazonDynamoDBClientBuilder.defaultClient()

    private val keyType: KeyType
    private val keyName: String
    private val attributes: MutableList<Field> = mutableListOf()
    private val tableName: String

    init {
        val keyValueStoreAnnotation = valueClass.getDeclaredAnnotation(KeyValueStore::class.java)
        tableName = if (keyValueStoreAnnotation.tableName != "") keyValueStoreAnnotation.tableName else valueClass.simpleName
        keyType = keyValueStoreAnnotation.keyType
        keyName = keyValueStoreAnnotation.keyName

        checkKeyIsCorrectType()

        for (field in valueClass.declaredFields) {
            if (field.isAnnotationPresent(Attribute::class.java)) {
                attributes.add(field)
                if (field.name == keyName) throw AttributeNameException()
            }
        }
    }


    fun put(key: K, value: V) {
        val attributeMap: MutableMap<String, AttributeValue> = mutableMapOf()

        for (attribute in attributes) {
            attribute.isAccessible = true
            attributeMap[attribute.name] = toAttributeValue(attribute.get(value))
        }

        attributeMap[keyName] = toAttributeValue(key)

        val putItemRequest = PutItemRequest().withItem(attributeMap).withTableName(tableName)

        client.putItem(putItemRequest)
    }

    fun delete(keyObj: K) {

        val convertedValue = toAttributeValue(keyObj)

        val deleteItemRequest = DeleteItemRequest()
                .withKey(keyToKeyMap(keyObj))
                .withTableName(tableName)

        client.deleteItem(deleteItemRequest)
    }

    fun getAll(): Map<K, V> {
        val scanRequest = ScanRequest()
                .withTableName(tableName)
        val scanResult = client.scan(scanRequest)

        val resultMap: MutableMap<K, V> = mutableMapOf()
        for (item in scanResult.items) {
            val key: K = keyFromAttributeValue(item[keyName]!!) as K
            resultMap[key] = toObject(item)
        }
        return resultMap
    }

    fun get(keyObj: K): V? {

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

    private fun toAttributeValue(value: Any?): AttributeValue {
        return when (value) {
            is String -> AttributeValue(value)
            is Number -> AttributeValue().withN(value.toString())
            is Boolean -> AttributeValue().withBOOL(value)
            else -> AttributeValue()
        }
    }

    private fun checkKeyIsCorrectType() {
        if (keyClass == String::class.java) {
            if (keyType != KeyType.STRING) throw MismatchedKeyException(keyType.name)
        } else if (Number::class.java.isAssignableFrom(keyClass)) {
            if (keyType != KeyType.NUMBER) throw MismatchedKeyException(keyType.name)
        } else if (keyClass == Boolean::class.java) {
            if (keyType != KeyType.BOOLEAN) throw MismatchedKeyException(keyType.name)
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

    private fun keyFromAttributeValue(value: AttributeValue): Any {
        return when {
            value.bool != null -> value.bool
            value.n != null -> {
                when (keyClass) {
                    Integer::class.java -> value.n.toInt()
                    Double::class.java -> value.n.toDouble()
                    Long::class.java -> value.n.toLong()
                    Float::class.java -> value.n.toFloat()
                    else -> value.n.toInt()
                }
            }
            value.s != null -> value.s
            else -> Any()
        }
    }

    private fun keyToKeyMap(keyObj: K): Map<String, AttributeValue> {
        val keyMap: MutableMap<String, AttributeValue> = mutableMapOf()

        keyMap[keyName] = toAttributeValue(keyObj)

        return keyMap
    }

    private fun toObject(map: MutableMap<String, AttributeValue>): V {
        map.remove(keyName)
        val convertedMap = map.mapValues { entry -> fromAttributeValue(entry.value) }

        val mapper = ObjectMapper()
        return mapper.convertValue(convertedMap, valueClass)
    }
}