package com.nimbusframework.nimbusaws.clients.document

import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.nimbusframework.nimbusaws.clients.dynamo.DynamoClient
import com.nimbusframework.nimbuscore.clients.document.AbstractDocumentStoreClient
import com.nimbusframework.nimbuscore.clients.store.ReadItemRequest
import com.nimbusframework.nimbuscore.clients.store.UpdateCondition
import com.nimbusframework.nimbuscore.clients.store.WriteItemRequest
import javax.naming.InvalidNameException

internal class DocumentStoreClientDynamo<T>(private val clazz: Class<T>, stage: String): AbstractDocumentStoreClient<T>(clazz, stage) {

    private val dynamoClient: DynamoClient<T> = DynamoClient(tableName, clazz, columnNames)

    override fun put(obj: T) {
        dynamoClient.put(obj, allAttributes)
    }

    override fun delete(obj: T) {
        dynamoClient.deleteKey(objectToKeyMap(obj))
    }

    override fun deleteKey(keyObj: Any) {
        dynamoClient.deleteKey(keyToKeyMap(keyObj))
    }

    override fun getAll(): List<T> {
        return dynamoClient.getAll().map { valueMap -> dynamoClient.toObject(valueMap, allAttributes) }
    }

    override fun get(keyObj: Any): T? {
        return dynamoClient.get(keyToKeyMap(keyObj), allAttributes)
    }

    override fun getReadItem(keyObj: Any): ReadItemRequest<T> {
        return dynamoClient.getReadItem(keyToKeyMap(keyObj), allAttributes)
    }

    override fun getWriteItem(obj: T): WriteItemRequest {
        return dynamoClient.getWriteItem(obj, allAttributes)
    }

    override fun getIncrementValueRequest(keyObj: Any, numericFieldName: String, amount: Number): WriteItemRequest {
        val columnName = columnNames[numericFieldName]
        if (columnName.isNullOrEmpty()) throw InvalidNameException("$numericFieldName is not a field of ${clazz.canonicalName}")
        return dynamoClient.getUpdateValueRequest(keyToKeyMap(keyObj), columnName, amount, "+")
    }

    override fun getDecrementValueRequest(keyObj: Any, numericFieldName: String, amount: Number): WriteItemRequest {
        val columnName = columnNames[numericFieldName]
        if (columnName.isNullOrEmpty()) throw InvalidNameException("$numericFieldName is not a field of ${clazz.canonicalName}")
        return dynamoClient.getUpdateValueRequest(keyToKeyMap(keyObj), columnName, amount, "-")
    }

    override fun getIncrementValueRequest(keyObj: Any, numericFieldName: String, amount: Number, updateCondition: UpdateCondition): WriteItemRequest {
        val columnName = columnNames[numericFieldName]
        if (columnName.isNullOrEmpty()) throw InvalidNameException("$numericFieldName is not a field of ${clazz.canonicalName}")
        return dynamoClient.getUpdateValueRequest(keyToKeyMap(keyObj), columnName, amount, "+", updateCondition)
    }

    override fun getDecrementValueRequest(keyObj: Any, numericFieldName: String, amount: Number, updateCondition: UpdateCondition): WriteItemRequest {
        val columnName = columnNames[numericFieldName]
        if (columnName.isNullOrEmpty()) throw InvalidNameException("$numericFieldName is not a field of ${clazz.canonicalName}")
        return dynamoClient.getUpdateValueRequest(keyToKeyMap(keyObj), columnName, amount, "-", updateCondition)
    }

    override fun getDeleteItemRequest(keyObj: Any): WriteItemRequest {
        return dynamoClient.getDeleteRequest(keyToKeyMap(keyObj))
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