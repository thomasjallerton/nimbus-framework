package com.nimbusframework.nimbusaws.clients.document

import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.nimbusframework.nimbusaws.clients.dynamo.DynamoClient
import com.nimbusframework.nimbuscore.clients.document.AbstractDocumentStoreClient
import com.nimbusframework.nimbuscore.clients.store.ReadItemRequest
import com.nimbusframework.nimbuscore.clients.store.conditions.ComparisionCondition
import com.nimbusframework.nimbuscore.clients.store.WriteItemRequest
import com.nimbusframework.nimbuscore.clients.store.conditions.Condition
import javax.naming.InvalidNameException

internal class DocumentStoreClientDynamo<T>(clazz: Class<T>, stage: String): AbstractDocumentStoreClient<T>(clazz, stage) {
    private val dynamoClient: DynamoClient<T> = DynamoClient(tableName, clazz, columnNames, allAttributes)

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

    override fun getAll(): List<T> {
        return dynamoClient.getAll().map { valueMap -> dynamoClient.toObject(valueMap) }
    }

    override fun get(keyObj: Any): T? {
        return dynamoClient.get(keyToKeyMap(keyObj))
    }

    override fun getReadItem(keyObj: Any): ReadItemRequest<T> {
        return dynamoClient.getReadItem(keyToKeyMap(keyObj))
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

    private fun objectToKeyMap(obj: T): Map<String, AttributeValue> {
        val keyMap: MutableMap<String, AttributeValue> = mutableMapOf()

        for ((columnName, field) in keys) {
            field.isAccessible = true
            keyMap[columnName] = dynamoClient.toAttributeValue(field.get(obj))
        }
        return keyMap
    }

    private fun keyToKeyMap(keyObj: Any): Map<String, AttributeValue> {
        if (keys.size > 1) {
            throw Exception("Composite key shouldn't exist!!")
        } else if (keys.isEmpty()) {
            throw Exception("Need a key field!")
        }

        val keyMap: MutableMap<String, AttributeValue> = mutableMapOf()

        for ((columnName, _) in keys) {
            keyMap[columnName] = dynamoClient.toAttributeValue(keyObj)
        }
        return keyMap
    }

}