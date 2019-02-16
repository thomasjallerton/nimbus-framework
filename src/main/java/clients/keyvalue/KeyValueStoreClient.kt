package clients.keyvalue

import annotation.annotations.keyvalue.KeyType
import annotation.annotations.keyvalue.KeyValueStore
import annotation.annotations.persistent.Attribute
import clients.dynamo.MismatchedKeyTypeException
import java.lang.reflect.Field

abstract class KeyValueStoreClient<K, V>(private val keyClass: Class<K>, valueClass: Class<V>) {

    private val keyType: KeyType
    protected val keyName: String
    protected val attributes: MutableMap<String, Field> = mutableMapOf()
    protected val tableName: String

    init {
        val keyValueStoreAnnotation = valueClass.getDeclaredAnnotation(KeyValueStore::class.java)
        tableName = if (keyValueStoreAnnotation.tableName != "") keyValueStoreAnnotation.tableName else valueClass.simpleName
        keyType = keyValueStoreAnnotation.keyType
        keyName = keyValueStoreAnnotation.keyName

        checkKeyIsCorrectType()

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

    private fun checkKeyIsCorrectType() {
        if (keyClass == String::class.java) {
            if (keyType != KeyType.STRING) throw MismatchedKeyTypeException(KeyType.STRING, keyClass)
        } else if (keyType == KeyType.NUMBER) {
            if (!Number::class.java.isAssignableFrom(keyClass) && keyClass != Int::class.java &&
                    keyClass != Double::class.java && keyClass != Float::class.java &&
                    keyClass != Long::class.java   && keyClass != Short::class.java) {
                throw MismatchedKeyTypeException(KeyType.NUMBER, keyClass)
            }
        } else if (keyClass == Boolean::class.java) {
            if (keyType != KeyType.BOOLEAN) throw MismatchedKeyTypeException(KeyType.BOOLEAN, keyClass)
        }
    }

    internal companion object {
        fun <T> getTableName(clazz: Class<T>): String {
            val keyValueStoreAnnotation = clazz.getDeclaredAnnotation(KeyValueStore::class.java)
            return if (keyValueStoreAnnotation.tableName != "") keyValueStoreAnnotation.tableName else clazz.simpleName
        }
    }
}