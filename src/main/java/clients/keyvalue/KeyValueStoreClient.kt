package clients.keyvalue

import annotation.annotations.keyvalue.KeyValueStore
import annotation.annotations.persistent.Attribute
import clients.dynamo.MismatchedKeyTypeException
import java.lang.reflect.Field

abstract class KeyValueStoreClient<K, V>(keyClass: Class<K>, valueClass: Class<V>) {

    private val keyType: Class<out Any>
    protected val keyName: String
    protected val attributes: MutableMap<String, Field> = mutableMapOf()
    protected val tableName: String

    init {
        val keyValueStoreAnnotation = valueClass.getDeclaredAnnotation(KeyValueStore::class.java)
        tableName = if (keyValueStoreAnnotation.tableName != "") keyValueStoreAnnotation.tableName else valueClass.simpleName
        keyType = keyValueStoreAnnotation.keyType.java

        if (keyType != keyClass) throw MismatchedKeyTypeException(keyType, keyClass)

        keyName = keyValueStoreAnnotation.keyName

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
        fun <T> getTableName(clazz: Class<T>): String {
            val keyValueStoreAnnotation = clazz.getDeclaredAnnotation(KeyValueStore::class.java)
            return if (keyValueStoreAnnotation.tableName != "") keyValueStoreAnnotation.tableName else clazz.simpleName
        }
    }
}