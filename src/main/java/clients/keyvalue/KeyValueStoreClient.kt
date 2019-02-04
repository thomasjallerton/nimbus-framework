package clients.keyvalue

import annotation.annotations.keyvalue.KeyType
import annotation.annotations.keyvalue.KeyValueStore
import annotation.annotations.persistent.Attribute
import clients.dynamo.DynamoClient
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.model.*
import com.fasterxml.jackson.databind.ObjectMapper
import java.lang.reflect.Field
import kotlin.reflect.full.superclasses

class KeyValueStoreClient<K, V>(private val keyClass: Class<K> , private val valueClass: Class<V>) {

    private val client = AmazonDynamoDBClientBuilder.defaultClient()
    private val dynamoClient: DynamoClient<V>

    private val keyType: KeyType
    private val keyName: String
    private val attributes: MutableList<Field> = mutableListOf()
    private val tableName: String

    init {
        val keyValueStoreAnnotation = valueClass.getDeclaredAnnotation(KeyValueStore::class.java)
        tableName = if (keyValueStoreAnnotation.tableName != "") keyValueStoreAnnotation.tableName else valueClass.simpleName
        keyType = keyValueStoreAnnotation.keyType
        keyName = keyValueStoreAnnotation.keyName
        dynamoClient = DynamoClient(tableName, valueClass)

        checkKeyIsCorrectType()

        for (field in valueClass.declaredFields) {
            if (field.isAnnotationPresent(Attribute::class.java)) {
                attributes.add(field)
                if (field.name == keyName) throw AttributeNameException()
            }
        }
    }


    fun put(key: K, value: V) {
        dynamoClient.put(value, attributes, mapOf(Pair(keyName, dynamoClient.toAttributeValue(key))))
    }

    fun delete(keyObj: K) {
        dynamoClient.deleteKey(keyToKeyMap(keyObj))
    }

    fun getAll(): Map<K, V> {
        val listAll = dynamoClient.getAll()

        val resultMap: MutableMap<K, V> = mutableMapOf()
        for (item in listAll) {
            val key: K = keyFromAttributeValue(item[keyName]!!) as K
            resultMap[key] = toObject(item)
        }
        return resultMap
    }

    fun get(keyObj: K): V? {
        return dynamoClient.get(keyToKeyMap(keyObj))
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

        keyMap[keyName] = dynamoClient.toAttributeValue(keyObj)

        return keyMap
    }

    private fun toObject(map: MutableMap<String, AttributeValue>): V {
        map.remove(keyName)
        val convertedMap = map.mapValues { entry -> dynamoClient.fromAttributeValue(entry.value) }

        val mapper = ObjectMapper()
        return mapper.convertValue(convertedMap, valueClass)
    }
}