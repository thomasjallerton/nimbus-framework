package clients.dynamo

import annotation.annotations.persistent.Attribute
import annotation.annotations.persistent.Key
import com.fasterxml.jackson.databind.ObjectMapper
import java.lang.reflect.Field

class DynamoStreamParser<T>(private val clazz: Class<T>) {

    private val keys: MutableMap<String, Field> = mutableMapOf()
    private val allAttributes: MutableMap<String, Field> = mutableMapOf()
    private val objectMapper = ObjectMapper()

    init {
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

    private fun <K> fromAttributeValue(value: Map<String, String>, expectedType: Class<K>, fieldName: String): Any? {
        return when {
            value.containsKey("Bool") && expectedType == Boolean::class.java -> value.getValue("Bool").toBoolean()
            value.containsKey("N") -> {
                when (expectedType) {
                    Integer::class.java -> value.getValue("N").toInt()
                    Double::class.java -> value.getValue("N").toDouble()
                    Long::class.java -> value.getValue("N").toLong()
                    Float::class.java -> value.getValue("N").toFloat()
                    else -> value.getValue("N").toInt()
                }
            }
            value.containsKey("S") && expectedType == String::class.java -> value.getValue("S")
            value.containsKey("S") -> objectMapper.readValue(value.getValue("S"), expectedType)
            else -> throw MismatchedTypeException(expectedType.simpleName, fieldName)
        }
    }

    fun toObject(obj: Map<String, Map<String, String>>): T {
        val resultMap: MutableMap<String, Any?> = mutableMapOf()

        for ((columnName, field) in allAttributes) {
            val attributeVal = obj[columnName]
            if (attributeVal != null) {
                resultMap[field.name] = fromAttributeValue(attributeVal, field.type, field.name)
            }
        }

        return objectMapper.convertValue(resultMap, clazz)
    }

}