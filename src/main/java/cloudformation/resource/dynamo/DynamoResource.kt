package cloudformation.resource.dynamo

import cloudformation.persisted.NimbusState
import cloudformation.resource.Resource
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import javax.lang.model.element.TypeElement

class DynamoResource(
        private val tableName: String,
        nimbusState: NimbusState
) : Resource(nimbusState) {

    private val keys: MutableList<Attribute> = mutableListOf()
    private val attributes: MutableList<Attribute> = mutableListOf()


    override fun toCloudFormation(): JsonObject {
        val dynamoTable = JsonObject()

        dynamoTable.addProperty("Type", "AWS::DynamoDB::Table")

        val properties = getProperties()
        properties.addProperty("TableName", tableName)
        properties.add("AttributeDefinitions", attributeDefsCloudFormation())
        properties.add("KeySchema", keyDefsCloudFormation())

        val provisionedThroughput = JsonObject()
        provisionedThroughput.addProperty("ReadCapacityUnits", 1)
        provisionedThroughput.addProperty("WriteCapacityUnits", 1)
        properties.add("ProvisionedThroughput", provisionedThroughput)

        dynamoTable.add("Properties", properties)
        return dynamoTable
    }

    override fun getName(): String {
        return tableName
    }

    private fun attributeDefsCloudFormation(): JsonArray {
        val attributeDefs = JsonArray()

        for (attribute in attributes) {
            val attributeJson = JsonObject()
            attributeJson.addProperty("AttributeName", attribute.name)
            attributeJson.addProperty("AttributeType", attribute.type)
            attributeDefs.add(attributeJson)
        }

        return attributeDefs
    }

    private fun keyDefsCloudFormation(): JsonArray {
        val keyDefs = JsonArray()

        for (key in keys) {
            val attributeJson = JsonObject()
            attributeJson.addProperty("AttributeName", key.name)
            attributeJson.addProperty("KeyType", key.type)
            keyDefs.add(attributeJson)
        }

        return keyDefs
    }

    fun addHashKey(name: String, type: Any) {
        keys.add(Attribute(name, "HASH"))
        attributes.add(Attribute(name, getDynamoType(type)))
    }

    fun <T> addHashKeyClass(name: String, type: TypeElement) {
        keys.add(Attribute(name, "HASH"))
        attributes.add(Attribute(name, getClassDynamoType(type)))
    }

    private fun getDynamoType(obj: Any): String {
        return when (obj) {
            is String -> "S"
            is Number -> "N"
            is Boolean -> "BOOL"
            else -> "S"
        }
    }

    private fun getClassDynamoType(obj: TypeElement): String {
        return when (obj.simpleName.toString()) {
            "String" -> "S"
            "Integer" -> "N"
            "Short" -> "N"
            "Byte" -> "N"
            "Char" -> "N"
            "Long" -> "N"
            "Float" -> "N"
            "Double" -> "N"
            "Boolean" -> "BOOL"
            else -> "S"
        }
    }

    private data class Attribute(val name: String, val type: String)
}