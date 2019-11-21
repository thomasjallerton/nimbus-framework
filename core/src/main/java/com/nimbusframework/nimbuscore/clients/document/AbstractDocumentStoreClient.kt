package com.nimbusframework.nimbuscore.clients.document

import com.nimbusframework.nimbuscore.annotations.persistent.Attribute
import com.nimbusframework.nimbuscore.annotations.persistent.Key
import com.nimbusframework.nimbuscore.clients.store.ItemDescription
import java.lang.reflect.Field

abstract class AbstractDocumentStoreClient<T>(clazz: Class<T>, tableName: String, stage: String): DocumentStoreClient<T> {

    protected val keys: MutableMap<String, Field> = mutableMapOf()
    protected val allAttributes: MutableMap<String, Field> = mutableMapOf()
    protected val userTableName: String = tableName.removeSuffix(stage)
    protected val columnNames: MutableMap<String, String> = mutableMapOf()

    init {
        for (field in clazz.declaredFields) {
            if (field.isAnnotationPresent(Key::class.java)) {
                val keyAnnotation = field.getDeclaredAnnotation(Key::class.java)
                val columnName = if (keyAnnotation.columnName != "") keyAnnotation.columnName else field.name
                keys[columnName] = field
                allAttributes[columnName] = field
                columnNames[field.name] = columnName
            } else if (field.isAnnotationPresent(Attribute::class.java)) {
                val attributeAnnotation = field.getDeclaredAnnotation(Attribute::class.java)
                val columnName = if (attributeAnnotation.columnName != "") attributeAnnotation.columnName else field.name
                allAttributes[columnName] = field
                columnNames[field.name] = columnName
            }
        }
    }

    fun getItemDescription(): ItemDescription {
        val key = keys.keys.first()
        val attributes = allAttributes.keys.filter { attribute -> attribute != key }.toSet()
        return ItemDescription(key, attributes)
    }
}