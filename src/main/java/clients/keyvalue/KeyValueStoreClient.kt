package clients.keyvalue

import annotation.annotations.keyvalue.KeyType
import annotation.annotations.keyvalue.KeyValueStore
import annotation.annotations.persistent.Attribute
import clients.dynamo.DynamoClient
import clients.dynamo.MismatchedKeyTypeException
import clients.dynamo.MismatchedTypeException
import com.amazonaws.services.dynamodbv2.model.*
import java.lang.reflect.Field

class KeyValueStoreClient<K, V>(private val keyClass: Class<K> , valueClass: Class<V>) {

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
            val key: K = dynamoClient.fromAttributeValue(item[keyName]!!, keyClass, keyName) as K
            resultMap[key] = toObject(item)
        }
        return resultMap
    }

    fun get(keyObj: K): V? {
        return dynamoClient.get(keyToKeyMap(keyObj))
    }

    private fun checkKeyIsCorrectType() {
        if (keyClass == String::class.java) {
            if (keyType != KeyType.STRING) throw MismatchedKeyTypeException(KeyType.STRING, keyClass)
        } else if (Number::class.java.isAssignableFrom(keyClass)) {
            if (keyType != KeyType.NUMBER) throw MismatchedKeyTypeException(KeyType.STRING, keyClass)
        } else if (keyClass == Boolean::class.java) {
            if (keyType != KeyType.BOOLEAN) throw MismatchedKeyTypeException(KeyType.STRING, keyClass)
        }
    }

    private fun keyToKeyMap(keyObj: K): Map<String, AttributeValue> {
        val keyMap: MutableMap<String, AttributeValue> = mutableMapOf()

        keyMap[keyName] = dynamoClient.toAttributeValue(keyObj)

        return keyMap
    }

    private fun toObject(map: MutableMap<String, AttributeValue>): V {
        map.remove(keyName)
        return dynamoClient.toObject(map)
    }
}