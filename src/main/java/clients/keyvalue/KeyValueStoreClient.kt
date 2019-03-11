package clients.keyvalue

import annotation.annotations.keyvalue.KeyValueStore
import annotation.annotations.persistent.Attribute
import clients.InvalidStageException
import clients.dynamo.MismatchedKeyTypeException
import java.lang.reflect.Field

abstract class KeyValueStoreClient<K, V>(keyClass: Class<K>, valueClass: Class<V>, stage: String) {

    private var keyType: Class<out Any> = String::class.java
    protected var keyName: String = ""
    protected val attributes: MutableMap<String, Field> = mutableMapOf()
    protected var tableName: String = ""

    init {
        val keyValueStoreAnnotations = valueClass.getAnnotationsByType(KeyValueStore::class.java)

        for (keyValueStoreAnnotation in keyValueStoreAnnotations) {
            for (annotationStage in keyValueStoreAnnotation.stages) {
                if (annotationStage == stage) {
                    tableName = if (keyValueStoreAnnotation.tableName != "") keyValueStoreAnnotation.tableName else valueClass.simpleName
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

    abstract fun put(key: K, value: V)

    abstract fun delete(keyObj: K)

    abstract fun getAll(): Map<K, V>

    abstract fun get(keyObj: K): V?

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
}