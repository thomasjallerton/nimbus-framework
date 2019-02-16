package clients.keyvalue

import testing.LocalNimbusDeployment

internal class KeyValueStoreClientLocal<K, V>(
        keyClass: Class<K>,
        valueClass: Class<V>
): KeyValueStoreClient<K, V>(keyClass, valueClass) {

    private val localDeployment = LocalNimbusDeployment.getInstance()
    private val table: MutableMap<Any?, Any?> = localDeployment.getKeyValueStore(valueClass)

    override fun put(key: K, value: V) {
        table[key] = value
    }

    override fun delete(keyObj: K) {
        table.remove(keyObj)
    }

    override fun getAll(): Map<K, V> {
        val result: MutableMap<K, V> = mutableMapOf()

        for (key in table.keys) {
            result[key as K] = table[key] as V
        }

        return result
    }

    override fun get(keyObj: K): V? {
        return table[keyObj] as V?
    }
}