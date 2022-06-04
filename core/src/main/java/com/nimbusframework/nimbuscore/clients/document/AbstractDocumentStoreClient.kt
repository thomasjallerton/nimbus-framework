package com.nimbusframework.nimbuscore.clients.document

import com.nimbusframework.nimbuscore.annotations.persistent.Attribute
import com.nimbusframework.nimbuscore.annotations.persistent.Key
import com.nimbusframework.nimbuscore.clients.store.ItemDescription
import java.lang.reflect.Field

abstract class AbstractDocumentStoreClient<T>(clazz: Class<T>, tableName: String, stage: String): DocumentStoreClient<T> {

    protected val key: Pair<String, Field>
    protected val allAttributes: MutableMap<String, Field> = mutableMapOf()
    protected val userTableName: String = tableName.removeSuffix(stage)
    protected val columnNames: MutableMap<String, String> = mutableMapOf()

    init {
        val keys = mutableMapOf<String, Field>()
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
        if (keys.size > 1) {
            throw Exception("Composite key shouldn't exist!!")
        } else if (keys.isEmpty()) {
            throw Exception("Need a key field!")
        }
        key = Pair(keys.keys.first(), keys.values.first())
    }

    fun getItemDescription(): ItemDescription {
        val attributes = allAttributes.keys.filter { attribute -> attribute != key.first }.toSet()
        return ItemDescription(key.first, attributes)
    }
}
