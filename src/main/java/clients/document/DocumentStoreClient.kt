package clients.document

import annotation.annotations.document.DocumentStore
import annotation.annotations.persistent.Attribute
import annotation.annotations.persistent.Key
import clients.InvalidStageException
import java.lang.reflect.Field

abstract class DocumentStoreClient<T>(clazz: Class<T>, stage: String) {

    protected val keys: MutableMap<String, Field> = mutableMapOf()
    protected val allAttributes: MutableMap<String, Field> = mutableMapOf()
    protected val tableName: String = getTableName(clazz, stage)

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

    abstract fun put(obj: T)

    abstract fun delete(obj: T)

    abstract fun deleteKey(keyObj: Any)

    abstract fun getAll(): List<T>

    abstract fun get(keyObj: Any): T?

    companion object {
        fun <T> getTableName(clazz: Class<T>, stage: String): String {
            val documentStoreAnnotations = clazz.getDeclaredAnnotationsByType(DocumentStore::class.java)
            for (documentStoreAnnotation in documentStoreAnnotations) {
                if (documentStoreAnnotation.stage == stage) {
                    val name = if (documentStoreAnnotation.tableName != "") documentStoreAnnotation.tableName else clazz.simpleName
                    return "$name$stage"
                }
            }
            throw InvalidStageException()
        }
    }
}