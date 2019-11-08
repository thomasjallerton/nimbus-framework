package com.nimbusframework.nimbuscore.clients.document

import com.nimbusframework.nimbuscore.annotations.document.DocumentStore
import com.nimbusframework.nimbuscore.annotations.persistent.Attribute
import com.nimbusframework.nimbuscore.annotations.persistent.Key
import com.nimbusframework.nimbuscore.clients.store.ItemDescription
import com.nimbusframework.nimbuscore.clients.store.ReadItemRequest
import com.nimbusframework.nimbuscore.clients.store.conditions.ComparisionCondition
import com.nimbusframework.nimbuscore.clients.store.WriteItemRequest
import com.nimbusframework.nimbuscore.clients.store.conditions.Condition
import com.nimbusframework.nimbuscore.exceptions.InvalidStageException
import java.lang.reflect.Field

abstract class AbstractDocumentStoreClient<T>(clazz: Class<T>, stage: String): DocumentStoreClient<T> {

    protected val keys: MutableMap<String, Field> = mutableMapOf()
    protected val allAttributes: MutableMap<String, Field> = mutableMapOf()
    protected val tableName: String = getTableName(clazz, stage)
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

    abstract override fun put(obj: T)

    abstract override fun delete(obj: T)

    abstract override fun deleteKey(keyObj: Any)

    abstract override fun getAll(): List<T>

    abstract override fun get(keyObj: Any): T?

    abstract override fun getReadItem(keyObj: Any): ReadItemRequest<T>

    abstract override fun getWriteItem(obj: T): WriteItemRequest

    abstract override fun getIncrementValueRequest(keyObj: Any, numericFieldName: String, amount: Number): WriteItemRequest

    abstract override fun getIncrementValueRequest(keyObj: Any, numericFieldName: String, amount: Number, condition: Condition): WriteItemRequest

    abstract override fun getDecrementValueRequest(keyObj: Any, numericFieldName: String, amount: Number): WriteItemRequest

    abstract override fun getDecrementValueRequest(keyObj: Any, numericFieldName: String, amount: Number, condition: Condition): WriteItemRequest

    abstract override fun getDeleteItemRequest(keyObj: Any): WriteItemRequest

    companion object {
        fun <T> getTableName(clazz: Class<T>, stage: String): String {
            val documentStoreAnnotations = clazz.getDeclaredAnnotationsByType(DocumentStore::class.java)
            for (documentStoreAnnotation in documentStoreAnnotations) {
                for (annotationStage in documentStoreAnnotation.stages) {
                    if (annotationStage == stage) {
                        val name =  if (documentStoreAnnotation.tableName != "") documentStoreAnnotation.tableName else clazz.simpleName
                        return "$name$stage"
                    }
                }
            }
            throw InvalidStageException()
        }
    }

    fun getItemDescription(): ItemDescription {
        val key = keys.keys.first()
        val attributes = allAttributes.keys.filter { attribute -> attribute != key }.toSet()
        return ItemDescription(key, attributes)
    }
}