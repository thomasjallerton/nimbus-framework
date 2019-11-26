package com.nimbusframework.nimbusaws.clients.keyvalue

import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.google.inject.Inject
import com.nimbusframework.nimbusaws.clients.dynamo.DynamoClient
import com.nimbusframework.nimbusaws.clients.dynamo.DynamoStreamParser
import com.nimbusframework.nimbuscore.clients.keyvalue.AbstractKeyValueStoreClient
import com.nimbusframework.nimbuscore.clients.store.ReadItemRequest
import com.nimbusframework.nimbuscore.clients.store.conditions.ComparisionCondition
import com.nimbusframework.nimbuscore.clients.store.WriteItemRequest
import com.nimbusframework.nimbuscore.clients.store.conditions.Condition
import javax.naming.InvalidNameException

internal class KeyValueStoreClientDynamo<K, V>(
        private val keyClass: Class<K>,
        valueClass: Class<V>,
        stage: String,
        keyName: String,
        tableName: String,
        keyType: Class<*>
): AbstractKeyValueStoreClient<K, V>(keyClass, valueClass, keyType, keyName, tableName, stage){

    private val dynamoStreamProcessor = DynamoStreamParser(valueClass, attributes)

    @Inject
    private lateinit var dynamoClientFactory: DynamoClient.DynamoClientFactory

    private val dynamoClient: DynamoClient by lazy { dynamoClientFactory.create(tableName, valueClass.canonicalName, columnNames) }

    override fun put(key: K, value: V) {
        dynamoClient.put(value, attributes, mapOf(Pair(keyName, dynamoClient.toAttributeValue(key))))
    }

    override fun put(key: K, value: V, condition: Condition) {
        dynamoClient.put(value, attributes, mapOf(Pair(keyName, dynamoClient.toAttributeValue(key))), condition)
    }

    override fun delete(keyObj: K) {
        dynamoClient.deleteKey(keyToKeyMap(keyObj))
    }

    override fun delete(keyObj: K, condition: Condition) {
        dynamoClient.deleteKey(keyToKeyMap(keyObj), condition)
    }

    override fun getAll(): Map<K, V> {
        val listAll = dynamoClient.getAll()

        val resultMap: MutableMap<K, V> = mutableMapOf()
        for (item in listAll) {
            val key: K = fromAttributeValue(item[keyName]!!, keyClass, keyName) as K
            resultMap[key] = toObject(item)
        }
        return resultMap
    }

    override fun get(keyObj: K): V? {
        return dynamoStreamProcessor.toObject(dynamoClient.get(keyToKeyMap(keyObj)))
    }

    override fun getReadItem(keyObj: K): ReadItemRequest<V> {
        return dynamoClient.getReadItem(keyToKeyMap(keyObj)) { dynamoStreamProcessor.toObject(it)!! }
    }

    override fun getWriteItem(key: K, value: V): WriteItemRequest {
        return dynamoClient.getWriteItem(value, attributes, mapOf(Pair(keyName, dynamoClient.toAttributeValue(key))))
    }

    override fun getWriteItem(key: K, value: V, condition: Condition): WriteItemRequest {
        return dynamoClient.getWriteItem(value, attributes, mapOf(Pair(keyName, dynamoClient.toAttributeValue(key))), condition)
    }

    override fun getIncrementValueRequest(key: K, numericFieldName: String, amount: Number): WriteItemRequest {
        return dynamoClient.getUpdateValueRequest(keyToKeyMap(key), numericFieldName, amount, "+")
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

    override fun getDeleteItemRequest(key: K, condition: Condition): WriteItemRequest {
        return dynamoClient.getDeleteRequest(keyToKeyMap(key), condition)
    }

    private fun keyToKeyMap(keyObj: K): Map<String, AttributeValue> {
        val keyMap: MutableMap<String, AttributeValue> = mutableMapOf()

        keyMap[keyName] = dynamoClient.toAttributeValue(keyObj)

        return keyMap
    }

    private fun <K> fromAttributeValue(value: AttributeValue, expectedType: Class<K>, fieldName: String): Any? {
        return dynamoStreamProcessor.fromAttributeValue(value, expectedType, fieldName)
    }

    private fun toObject(obj: Map<String, AttributeValue>): V {
        return dynamoStreamProcessor.toObject(obj)!!
    }

}