package testing.keyvalue

import clients.keyvalue.KeyValueStoreClient
import com.fasterxml.jackson.databind.ObjectMapper
import testing.document.KeyValueMethod

class LocalKeyValueStore<K, V>(keyClass: Class<K>, private val valueClass: Class<V>): KeyValueStoreClient<K, V>(keyClass, valueClass) {

    private val keyValueStore: MutableMap<K, String> = mutableMapOf()
    private val objectMapper = ObjectMapper()
    private val methods: MutableList<KeyValueMethod> = mutableListOf()

    override fun put(key: K, value: V) {
        val valueStr = allAttributesToJson(value)

        if (keyValueStore.containsKey(key)) {
            val oldItem = objectMapper.readValue(keyValueStore[key], valueClass)
            keyValueStore[key] = valueStr
            methods.forEach {method -> method.invokeModify(oldItem, value)}

        } else {
            keyValueStore[key] = valueStr
            methods.forEach {method -> method.invokeInsert(value)}
        }
    }

    override fun delete(keyObj: K) {
        if (keyValueStore.containsKey(keyObj)) {
            val removedItemStr = keyValueStore.remove(keyObj)
            val oldItem = objectMapper.readValue(removedItemStr, valueClass)
            methods.forEach {method -> method.invokeRemove(oldItem)}
        }
    }

    override fun get(keyObj: K): V? {
        val strValue = keyValueStore[keyObj]
        return if (strValue != null) {
            objectMapper.readValue(strValue, valueClass)
        } else {
            null
        }
    }

    fun size(): Int {return keyValueStore.size}

    internal fun addMethod(method: KeyValueMethod) {
        methods.add(method)
    }


    override fun getAll(): Map<K, V> {
        return keyValueStore.mapValues { entry -> objectMapper.readValue(entry.value, valueClass)}
    }

    private fun allAttributesToJson(obj: V): String {
        val attributeMap: MutableMap<String, Any?> = mutableMapOf()

        for ((fieldName, field) in attributes) {
            field.isAccessible = true
            attributeMap[fieldName] = field.get(obj)
        }

        return objectMapper.writeValueAsString(attributeMap)
    }

}