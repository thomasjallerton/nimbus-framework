package com.nimbusframework.nimbusaws.clients.document

import com.nimbusframework.nimbusaws.clients.dynamo.DynamoClient
import com.nimbusframework.nimbusaws.clients.dynamo.DynamoStreamParser
import com.nimbusframework.nimbuscore.clients.document.AbstractDocumentStoreClient
import com.nimbusframework.nimbuscore.clients.store.ReadItemRequest
import com.nimbusframework.nimbuscore.clients.store.WriteItemRequest
import com.nimbusframework.nimbuscore.clients.store.conditions.Condition
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import java.util.stream.Stream

internal class DocumentStoreClientDynamo<T>(
        clazz: Class<T>,
        tableName: String,
        stage: String,
        dynamoClientFactory: (Map<String, String>, String) -> DynamoClient
): AbstractDocumentStoreClient<T>(clazz, tableName, stage) {

    private val dynamoStreamProcessor = DynamoStreamParser(clazz, allAttributes)

    private val dynamoClient = dynamoClientFactory(columnNames, key.first)

    override fun put(obj: T) {
        dynamoClient.put(obj, allAttributes)
    }

    override fun put(obj: T, condition: Condition) {
        dynamoClient.put(obj, allAttributes, condition = condition)
    }

    override fun delete(obj: T) {
        dynamoClient.deleteKey(objectToKeyMap(obj))
    }

    override fun delete(obj: T, condition: Condition) {
        dynamoClient.deleteKey(objectToKeyMap(obj), condition)
    }

    override fun deleteKey(keyObj: Any) {
        dynamoClient.deleteKey(keyToKeyMap(keyObj))
    }

    override fun deleteKey(keyObj: Any, condition: Condition) {
        dynamoClient.deleteKey(keyToKeyMap(keyObj), condition)
    }

    override fun getAll(): Stream<T> {
        return dynamoClient.getAll().map { valueMap -> toObject(valueMap) }
    }

    override fun getAllKeys(): Stream<Any> {
        return dynamoClient.getAllKeys().map { valueMap -> dynamoStreamProcessor.fromAttributeValue(valueMap.values.first(), key.second.type, key.first) }
    }

    override fun get(keyObj: Any): T? {
        return dynamoStreamProcessor.toObject(dynamoClient.get(keyToKeyMap(keyObj)))
    }

    override fun getReadItem(keyObj: Any): ReadItemRequest<T> {
        return dynamoClient.getReadItem(keyToKeyMap(keyObj)) { dynamoStreamProcessor.toObject(it)!! }
    }

    override fun getWriteItem(obj: T): WriteItemRequest {
        return dynamoClient.getWriteItem(obj, allAttributes)
    }

    override fun getWriteItem(obj: T, condition: Condition): WriteItemRequest {
        return dynamoClient.getWriteItem(obj, allAttributes, condition = condition)
    }

    override fun getIncrementValueRequest(keyObj: Any, numericFieldName: String, amount: Number): WriteItemRequest {
        return dynamoClient.getUpdateValueRequest(keyToKeyMap(keyObj), numericFieldName, amount, "+")
    }

    override fun getDecrementValueRequest(keyObj: Any, numericFieldName: String, amount: Number): WriteItemRequest {
        return dynamoClient.getUpdateValueRequest(keyToKeyMap(keyObj), numericFieldName, amount, "-")
    }

    override fun getIncrementValueRequest(keyObj: Any, numericFieldName: String, amount: Number, condition: Condition): WriteItemRequest {
        return dynamoClient.getUpdateValueRequest(keyToKeyMap(keyObj), numericFieldName, amount, "+", condition)
    }

    override fun getDecrementValueRequest(keyObj: Any, numericFieldName: String, amount: Number, condition: Condition): WriteItemRequest {
        return dynamoClient.getUpdateValueRequest(keyToKeyMap(keyObj), numericFieldName, amount, "-", condition)
    }

    override fun getDeleteKeyItemRequest(keyObj: Any): WriteItemRequest {
        return dynamoClient.getDeleteRequest(keyToKeyMap(keyObj))
    }

    override fun getDeleteKeyItemRequest(keyObj: Any, condition: Condition): WriteItemRequest {
        return dynamoClient.getDeleteRequest(keyToKeyMap(keyObj), condition)
    }

    override fun getDeleteItemRequest(obj: T): WriteItemRequest {
        return dynamoClient.getDeleteRequest(objectToKeyMap(obj))
    }

    override fun getDeleteItemRequest(obj: T, condition: Condition): WriteItemRequest {
        return dynamoClient.getDeleteRequest(objectToKeyMap(obj), condition)
    }

    override fun filter(attributeCondition: Condition): Stream<T> {
        return dynamoClient.filter(attributeCondition).map { toObject(it) }
    }

    private fun objectToKeyMap(obj: T): Map<String, AttributeValue> {
        val keyMap: MutableMap<String, AttributeValue> = mutableMapOf()
        key.second.isAccessible = true
        keyMap[key.first] = dynamoClient.toAttributeValue(key.second.get(obj))
        return keyMap
    }

    private fun keyToKeyMap(keyObj: Any): Map<String, AttributeValue> {
        val keyMap: MutableMap<String, AttributeValue> = mutableMapOf()
        keyMap[key.first] = dynamoClient.toAttributeValue(keyObj)
        return keyMap
    }

    private fun toObject(obj: Map<String, AttributeValue>): T {
        return dynamoStreamProcessor.toObject(obj)!!
    }
}
