package com.nimbusframework.nimbuscore.clients.keyvalue

import com.nimbusframework.nimbuscore.annotation.annotations.keyvalue.KeyValueStore
import com.nimbusframework.nimbuscore.annotation.annotations.persistent.Attribute
import com.nimbusframework.nimbuscore.clients.InvalidStageException
import com.nimbusframework.nimbuscore.clients.dynamo.MismatchedKeyTypeException
import com.nimbusframework.nimbuscore.testing.webserver.webconsole.models.ItemDescription
import java.lang.reflect.Field

abstract class AbstractKeyValueStoreClient<K, V>(keyClass: Class<K>, valueClass: Class<V>, stage: String): KeyValueStoreClient<K, V> {

    private var keyType: Class<out Any> = String::class.java
    protected var keyName: String = ""
    protected val attributes: MutableMap<String, Field> = mutableMapOf()
    protected var tableName: String = getTableName(valueClass, stage)
    protected val userTableName: String = tableName.removeSuffix(stage)


    init {
        val keyValueStoreAnnotations = valueClass.getAnnotationsByType(KeyValueStore::class.java)

        for (keyValueStoreAnnotation in keyValueStoreAnnotations) {
            for (annotationStage in keyValueStoreAnnotation.stages) {
                if (annotationStage == stage) {
                    keyType = keyValueStoreAnnotation.keyType.java
                    keyName = keyValueStoreAnnotation.keyName
                    break
                }
            }
        }

        if (keyType != keyClass) throw MismatchedKeyTypeException(keyType, keyClass)


        for (field in valueClass.declaredFields) {
            if (field.isAnnotationPresent(Attribute::class.java)) {
                val attributeAnnotation = field.getDeclaredAnnotation(Attribute::class.java)
                val columnName = if (attributeAnnotation.columnName != "") attributeAnnotation.columnName else field.name
                attributes[columnName] = field
                if (field.name == keyName) throw AttributeNameException()
            }
        }
    }

    abstract override fun put(key: K, value: V)

    abstract override fun delete(keyObj: K)

    abstract override fun getAll(): Map<K, V>

    abstract override fun get(keyObj: K): V?

    internal companion object {
        fun <T> getTableName(clazz: Class<T>, stage: String): String {
            val keyValueStoreAnnotations = clazz.getAnnotationsByType(KeyValueStore::class.java)
            for (keyValueStoreAnnotation in keyValueStoreAnnotations) {
                for (annotationStage in keyValueStoreAnnotation.stages) {
                    if (annotationStage == stage) {
                        val name = if (keyValueStoreAnnotation.tableName != "") keyValueStoreAnnotation.tableName else clazz.simpleName
                        return "$name$stage"
                    }
                }
            }
            throw InvalidStageException()
        }
    }

    internal fun getItemDescription(): ItemDescription {
        val attributes = attributes.keys.filter { attribute -> attribute != keyName }.toSet()
        return ItemDescription(keyName, attributes)
    }
}