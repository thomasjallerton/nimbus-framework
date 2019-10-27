package com.nimbusframework.nimbusaws.clients.keyvalue

import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.nimbusframework.nimbusaws.clients.dynamo.DynamoClient
import com.nimbusframework.nimbuscore.clients.keyvalue.AbstractKeyValueStoreClient

internal class KeyValueStoreClientDynamo<K, V>(
        private val keyClass: Class<K>,
        valueClass: Class<V>,
        stage: String
): AbstractKeyValueStoreClient<K, V>(keyClass, valueClass, stage){

    private val dynamoClient: DynamoClient<V> = DynamoClient(tableName, valueClass)

    override fun put(key: K, value: V) {
        dynamoClient.put(value, attributes, mapOf(Pair(keyName, dynamoClient.toAttributeValue(key))))
    }

    override fun delete(keyObj: K) {
        dynamoClient.deleteKey(keyToKeyMap(keyObj))
    }

    override fun getAll(): Map<K, V> {
        val listAll = dynamoClient.getAll()

        val resultMap: MutableMap<K, V> = mutableMapOf()
        for (item in listAll) {
            val key: K = dynamoClient.fromAttributeValue(item[keyName]!!, keyClass, keyName) as K
            resultMap[key] = dynamoClient.toObject(item, attributes)
        }
        return resultMap
    }

    override fun get(keyObj: K): V? {
        return dynamoClient.get(keyToKeyMap(keyObj), attributes)
    }

    private fun keyToKeyMap(keyObj: K): Map<String, AttributeValue> {
        val keyMap: MutableMap<String, AttributeValue> = mutableMapOf()

        keyMap[keyName] = dynamoClient.toAttributeValue(keyObj)

        return keyMap
    }
}