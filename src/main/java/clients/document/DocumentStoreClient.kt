package clients.document

import annotation.annotations.persistent.Attribute
import annotation.annotations.persistent.Key
import annotation.annotations.document.DocumentStore
import clients.dynamo.DynamoClient
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.model.*
import com.fasterxml.jackson.databind.ObjectMapper
import java.lang.reflect.Field

class DocumentStoreClient<T>(clazz: Class<T>) {

    private val dynamoClient: DynamoClient<T>

    private val keys: MutableList<Field> = mutableListOf()
    private val allAttributes: MutableList<Field> = mutableListOf()
    private val tableName: String

    init {
        val documentStoreAnnotation = clazz.getDeclaredAnnotation(DocumentStore::class.java)
        tableName = if (documentStoreAnnotation.tableName != "") documentStoreAnnotation.tableName else clazz.simpleName
        dynamoClient = DynamoClient(tableName, clazz)

        for (field in clazz.declaredFields) {
            if (field.isAnnotationPresent(Key::class.java)) {
                keys.add(field)
                allAttributes.add(field)
            } else if (field.isAnnotationPresent(Attribute::class.java)) {
                allAttributes.add(field)
            }
        }
    }


    fun put(obj: T) {
        dynamoClient.put(obj, allAttributes)
    }

    fun delete(obj: T) {
        dynamoClient.deleteKey(objectToKeyMap(obj))
    }

    fun deleteKey(keyObj: Any) {
        dynamoClient.deleteKey(keyToKeyMap(keyObj))
    }

    fun getAll(): List<T> {
        return dynamoClient.getAll().map { valueMap -> dynamoClient.toObject(valueMap) }
    }

    fun get(keyObj: Any): T? {
        return dynamoClient.get(keyToKeyMap(keyObj))
    }

    private fun objectToKeyMap(obj: T): Map<String, AttributeValue> {
        val keyMap: MutableMap<String, AttributeValue> = mutableMapOf()

        for (key in keys) {
            key.isAccessible = true
            keyMap[key.name] = dynamoClient.toAttributeValue(key.get(obj))
        }
        return keyMap
    }

    private fun keyToKeyMap(keyObj: Any): Map<String, AttributeValue> {
        if (keys.size > 1) {
            throw Exception("Composite key shouldn't exist!!")
        }
        val keyMap: MutableMap<String, AttributeValue> = mutableMapOf()

        for (key in keys) {
            keyMap[key.name] = dynamoClient.toAttributeValue(keyObj)
        }
        return keyMap
    }

}