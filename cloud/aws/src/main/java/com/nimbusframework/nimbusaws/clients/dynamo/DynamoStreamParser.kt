package com.nimbusframework.nimbusaws.clients.dynamo

import com.nimbusframework.nimbuscore.annotations.persistent.Attribute
import com.nimbusframework.nimbuscore.annotations.persistent.Key
import com.nimbusframework.nimbuscore.clients.JacksonClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import java.lang.reflect.Field

class DynamoStreamParser<T>(private val clazz: Class<T>, private val allAttributes: Map<String, Field>) {

    fun <K> fromAttributeValue(value: AttributeValue, expectedType: Class<K>, fieldName: String): Any? {
        return when {
            value.bool() != null && expectedType == Boolean::class.java -> value.bool()
            value.n() != null -> {
                when (expectedType) {
                    Integer::class.java -> value.n().toInt()
                    Double::class.java -> value.n().toDouble()
                    Long::class.java -> value.n().toLong()
                    Float::class.java -> value.n().toFloat()
                    else -> value.n().toInt()
                }
            }
            value.s() != null && expectedType == String::class.java -> value.s()
            value.s() != null -> JacksonClient.readValue(value.s(), expectedType)
            else -> throw MismatchedTypeException(expectedType.simpleName, fieldName)
        }
    }

    fun <K> fromAttributeValue(value: com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue, expectedType: Class<K>, fieldName: String): Any? {
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
            value.s != null -> JacksonClient.readValue(value.s, expectedType)
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

        return JacksonClient.convertValue(resultMap, clazz)
    }

    fun toObjectFromLambda(obj: Map<String, com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue>?): T? {
        if (obj == null) return null

        val resultMap: MutableMap<String, Any?> = mutableMapOf()

        for ((columnName, field) in allAttributes) {
            val attributeVal = obj[columnName]
            if (attributeVal != null) {
                resultMap[field.name] = fromAttributeValue(attributeVal, field.type, field.name)
            }
        }

        return JacksonClient.convertValue(resultMap, clazz)
    }

    companion object {

        @JvmStatic
        fun <T> of(clazz: Class<T>): DynamoStreamParser<T> {
            val allAttributes: MutableMap<String, Field> = mutableMapOf()
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
            return DynamoStreamParser(clazz, allAttributes)
        }
    }

}
