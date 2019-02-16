package clients.document

import annotation.annotations.document.DocumentStore
import annotation.annotations.persistent.Attribute
import annotation.annotations.persistent.Key
import clients.dynamo.DynamoClient
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import java.lang.reflect.Field

internal class DocumentStoreClientDynamo<T>(clazz: Class<T>): DocumentStoreClient<T> {

    private val dynamoClient: DynamoClient<T>

    private val keys: MutableMap<String, Field> = mutableMapOf()
    private val allAttributes: MutableMap<String, Field> = mutableMapOf()
    private val tableName: String

    init {
        val documentStoreAnnotation = clazz.getDeclaredAnnotation(DocumentStore::class.java)
        tableName = if (documentStoreAnnotation.tableName != "") documentStoreAnnotation.tableName else clazz.simpleName
        dynamoClient = DynamoClient(tableName, clazz)

        for (field in clazz.declaredFields) {
            if (field.isAnnotationPresent(Key::class.java)) {
                val keyAnnotation = field.getDeclaredAnnotation(Key::class.java)
                val columnName = if (keyAnnotation.columnName != "") keyAnnotation.columnName else field.name
                keys[columnName] = field
                allAttributes[columnName] = field
            } else if (field.isAnnotationPresent(Attribute::class.java)) {
                val attributeAnnotation = field.getDeclaredAnnotation(Attribute::class.java)
                val columnName = if (attributeAnnotation.columnName != "") attributeAnnotation.columnName else field.name
                allAttributes[columnName] = field
            }
        }
    }


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
        }
        val keyMap: MutableMap<String, AttributeValue> = mutableMapOf()

        for ((columnName, _) in keys) {
            keyMap[columnName] = dynamoClient.toAttributeValue(keyObj)
        }
        return keyMap
    }

}