package clients.dynamo

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.model.*
import com.fasterxml.jackson.databind.ObjectMapper
import java.lang.reflect.Field

class DynamoClient<T>(private val tableName: String, private val clazz: Class<T>) {

    private val client = AmazonDynamoDBClientBuilder.defaultClient()


    fun put(obj: T, allAttributes: List<Field>, additionalEntries:Map<String, AttributeValue> = mapOf()) {
        val attributeMap: MutableMap<String, AttributeValue> = mutableMapOf()

        for (attribute in allAttributes) {
            attribute.isAccessible = true
            attributeMap[attribute.name] = toAttributeValue(attribute.get(obj))
        }

        attributeMap.putAll(additionalEntries)

        val putItemRequest = PutItemRequest().withItem(attributeMap).withTableName(tableName)

        client.putItem(putItemRequest)
    }

    fun deleteKey(keyMap: Map<String, AttributeValue>) {

        val deleteItemRequest = DeleteItemRequest()
                .withKey(keyMap)
                .withTableName(tableName)

        client.deleteItem(deleteItemRequest)
    }

    fun getAll(): List<MutableMap<String, AttributeValue>> {
        val scanRequest = ScanRequest()
                .withTableName(tableName)
        val scanResult = client.scan(scanRequest)

        return scanResult.items
    }

    fun get(keyMap: Map<String, AttributeValue>): T? {

        val convertedMap = keyMap.mapValues { entry ->
            Condition().withComparisonOperator("EQ").withAttributeValueList(listOf(entry.value))
        }

        val queryRequest = QueryRequest()
                .withKeyConditions(convertedMap)
                .withTableName(tableName)

        val queryResult = client.query(queryRequest)

        return if (queryResult.count == 1) {
            toObject(queryResult.items[0])
        } else {
            null
        }
    }

    fun toAttributeValue(value: Any?): AttributeValue {
        return when (value) {
            is String -> AttributeValue(value)
            is Number -> AttributeValue().withN(value.toString())
            is Boolean -> AttributeValue().withBOOL(value)
            else -> AttributeValue()
        }
    }

    fun fromAttributeValue(value: AttributeValue): Any {
        return when {
            value.bool != null -> value.bool
            value.n != null -> value.n.toDouble()
            value.s != null -> value.s
            else -> Any()
        }
    }

    fun toObject(map: Map<String, AttributeValue>): T {
        val convertedMap = map.mapValues { entry -> fromAttributeValue(entry.value) }

        val mapper = ObjectMapper()
        return mapper.convertValue(convertedMap, clazz)
    }
}