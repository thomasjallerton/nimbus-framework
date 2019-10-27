package com.nimbusframework.nimbusaws.clients.dynamo

import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.nimbusframework.nimbuscore.annotations.persistent.Attribute
import com.nimbusframework.nimbuscore.annotations.persistent.Key
import com.fasterxml.jackson.databind.ObjectMapper
import java.lang.reflect.Field

class DynamoStreamParser<T>(private val clazz: Class<T>) {

    private val allAttributes: MutableMap<String, Field> = mutableMapOf()
    private val objectMapper = ObjectMapper()

    init {
        for (field in clazz.declaredFields) {
            if (field.isAnnotationPresent(Key::class.java)) {
                val keyAnnotation = field.getDeclaredAnnotation(Key::class.java)
                val columnName = if (keyAnnotation.columnName != "") keyAnnotation.columnName else field.name
                allAttributes[columnName] = field
            } else if (field.isAnnotationPresent(Attribute::class.java)) {
                val attributeAnnotation = field.getDeclaredAnnotation(Attribute::class.java)
                val columnName = if (attributeAnnotation.columnName != "") attributeAnnotation.columnName else field.name
                allAttributes[columnName] = field
            }
        }
    }

    private fun <K> fromAttributeValue(value: AttributeValue, expectedType: Class<K>, fieldName: String): Any? {
        return when {
            value.bool != null && expectedType == Boolean::class.java -> value.bool
            value.n != null -> {
                when (expectedType) {
                    Integer::class.java -> value.n.toInt()
                    Double::class.java -> value.n.toDouble()
                    Long::class.java -> value.n.toLong()
                    Float::class.java -> value.n.toFloat()
                    else -> value.n.toInt()
                }
            }
            value.s != null && expectedType == String::class.java -> value.s
            value.s != null -> objectMapper.readValue(value.s, expectedType)
            else -> throw MismatchedTypeException(expectedType.simpleName, fieldName)
        }
    }

    fun toObject(obj: Map<String, AttributeValue>?): T? {
        if (obj == null) return null

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