package clients.keyvalue

import annotation.annotations.keyvalue.Attribute
import annotation.annotations.keyvalue.Key
import annotation.annotations.keyvalue.KeyValueStore
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.PutItemRequest
import com.amazonaws.services.dynamodbv2.model.PutItemResult
import java.lang.Exception
import java.lang.reflect.Field

class KeyValueStoreClient {

    fun <T> save(obj: T, clazz: Class<T>): Boolean {
        val client = AmazonDynamoDBClientBuilder.defaultClient()

        try {
            val keyValueStoreAnnotation = clazz.getDeclaredAnnotation(KeyValueStore::class.java)
            val tableName = if (keyValueStoreAnnotation.tableName != "") keyValueStoreAnnotation.tableName else clazz.simpleName
            val keys: MutableList<Field> = mutableListOf()
            val attributes: MutableList<Field> = mutableListOf()

            for (field in clazz.declaredFields) {
                if (field.isAnnotationPresent(Key::class.java)) keys.add(field)
                if (field.isAnnotationPresent(Attribute::class.java)) attributes.add(field)
            }

            val attributeMap: MutableMap<String, AttributeValue> = mutableMapOf()

            for (key in keys) {
                key.isAccessible = true
                val attributeValue = convert(key.get(obj))
                attributeMap[key.name] = attributeValue
            }

            for (attribute in attributes) {
                attribute.isAccessible = true
                val attributeValue = convert(attribute.get(obj))
                attributeMap[attribute.name] = attributeValue
            }

            val putItemRequest = PutItemRequest().withItem(attributeMap).withTableName(tableName)

            client.putItem(putItemRequest)

            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun delete() {

    }

    fun scan() {

    }

//    fun <T: Any> query(key: Any): T {
//    }

    private fun convert(value: Any): AttributeValue {
        return when (value) {
            is String -> AttributeValue(value)
            is Number -> AttributeValue().withN(value.toString())
            is Boolean -> AttributeValue().withBOOL(value)
            else -> AttributeValue()
        }
    }

}