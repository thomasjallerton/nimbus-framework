package clients.keyvalue

import testing.LocalNimbusDeployment
import testing.keyvalue.LocalKeyValueStore

internal class KeyValueStoreClientLocal<K, V>(
        keyClass: Class<K>,
        valueClass: Class<V>,
        stage: String
): KeyValueStoreClient<K, V>(keyClass, valueClass, stage) {

    private val localDeployment = LocalNimbusDeployment.getInstance()
    private val table: LocalKeyValueStore<K, V> = localDeployment.getKeyValueStore(valueClass)

    override fun put(key: K, value: V) {
        table.put(key, value)
    }

    override fun delete(keyObj: K) {
        table.delete(keyObj)
    }

    override fun getAll(): Map<K, V> {
        return table.getAll()

    }

    override fun get(keyObj: K): V? {
        return table.get(keyObj)
    }
}