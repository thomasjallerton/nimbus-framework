package com.nimbusframework.nimbusaws.clients.keyvalue

import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.nimbusframework.nimbusaws.clients.dynamo.DynamoClient
import com.nimbusframework.nimbuscore.clients.keyvalue.AbstractKeyValueStoreClient
import com.nimbusframework.nimbuscore.clients.store.ReadItemRequest
import com.nimbusframework.nimbuscore.clients.store.conditions.ComparisionCondition
import com.nimbusframework.nimbuscore.clients.store.WriteItemRequest
import com.nimbusframework.nimbuscore.clients.store.conditions.Condition
import javax.naming.InvalidNameException

internal class KeyValueStoreClientDynamo<K, V>(
        private val keyClass: Class<K>,
        private val valueClass: Class<V>,
        stage: String
): AbstractKeyValueStoreClient<K, V>(keyClass, valueClass, stage){

    private val dynamoClient: DynamoClient<V> = DynamoClient(tableName, valueClass, columnNames)

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

    override fun getReadItem(keyObj: K): ReadItemRequest<V> {
        return dynamoClient.getReadItem(keyToKeyMap(keyObj), attributes)
    }

    override fun getWriteItem(key: K, value: V): WriteItemRequest {
        return dynamoClient.getWriteItem(value, attributes, mapOf(Pair(keyName, dynamoClient.toAttributeValue(key))))
    }

    override fun getIncrementValueRequest(key: K, numericFieldName: String, amount: Number): WriteItemRequest {
        val columnName = columnNames[numericFieldName]
        if (columnName.isNullOrEmpty()) throw InvalidNameException("$numericFieldName is not a field of ${valueClass.canonicalName}")
        return dynamoClient.getUpdateValueRequest(keyToKeyMap(key), columnName, amount, "+")
    }

    override fun getDecrementValueRequest(key: K, numericFieldName: String, amount: Number): WriteItemRequest {
        return dynamoClient.getUpdateValueRequest(keyToKeyMap(key), numericFieldName, amount, "-")
    }

    override fun getIncrementValueRequest(key: K, numericFieldName: String, amount: Number, condition: Condition): WriteItemRequest {
        return dynamoClient.getUpdateValueRequest(keyToKeyMap(key), numericFieldName, amount, "+", condition)
    }

    override fun getDecrementValueRequest(key: K, numericFieldName: String, amount: Number, condition: Condition): WriteItemRequest {
        return dynamoClient.getUpdateValueRequest(keyToKeyMap(key), numericFieldName, amount, "-", condition)
    }

    override fun getDeleteItemRequest(key: K): WriteItemRequest {
        return dynamoClient.getDeleteRequest(keyToKeyMap(key))
    }

    private fun keyToKeyMap(keyObj: K): Map<String, AttributeValue> {
        val keyMap: MutableMap<String, AttributeValue> = mutableMapOf()

        keyMap[keyName] = dynamoClient.toAttributeValue(keyObj)

        return keyMap
    }

}