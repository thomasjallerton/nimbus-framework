package com.nimbusframework.nimbuscore.clients.keyvalue

import com.nimbusframework.nimbuscore.annotations.persistent.Attribute
import com.nimbusframework.nimbuscore.clients.store.ItemDescription
import com.nimbusframework.nimbuscore.exceptions.AttributeNameException
import com.nimbusframework.nimbuscore.exceptions.MismatchedKeyTypeException
import java.lang.reflect.Field

abstract class AbstractKeyValueStoreClient<K, V>(
        keyClass: Class<K>,
        valueClass: Class<V>,
        keyType: Class<*>,
        protected val keyName: String,
        protected val tableName: String,
        stage: String): KeyValueStoreClient<K, V> {

    protected val attributes: MutableMap<String, Field> = mutableMapOf()
    protected val userTableName: String = tableName.removeSuffix(stage)
    protected val columnNames: MutableMap<String, String> = mutableMapOf()


    init {
        if (keyType != keyClass) throw MismatchedKeyTypeException(keyType, keyClass)

        for (field in valueClass.declaredFields) {
            if (field.isAnnotationPresent(Attribute::class.java)) {
                val attributeAnnotation = field.getDeclaredAnnotation(Attribute::class.java)
                val columnName = if (attributeAnnotation.columnName != "") attributeAnnotation.columnName else field.name
                attributes[columnName] = field
                columnNames[field.name] = columnName
                if (field.name == keyName) throw AttributeNameException(field.name)
            }
        }
    }

    fun getItemDescription(): ItemDescription {
        val attributes = attributes.keys.filter { attribute -> attribute != keyName }.toSet()
        return ItemDescription(keyName, attributes)
    }
}