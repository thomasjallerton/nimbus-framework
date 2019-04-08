package com.nimbusframework.nimbuscore.clients.document

import com.nimbusframework.nimbuscore.clients.dynamo.DynamoClient
import com.amazonaws.services.dynamodbv2.model.AttributeValue

internal class DocumentStoreClientDynamo<T>(clazz: Class<T>, stage: String): DocumentStoreClient<T>(clazz, stage) {

    private val dynamoClient: DynamoClient<T> = DynamoClient(tableName, clazz)

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