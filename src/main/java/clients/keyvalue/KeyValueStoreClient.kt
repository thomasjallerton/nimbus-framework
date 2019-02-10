package clients.keyvalue

interface KeyValueStoreClient<K, V> {

    fun put(key: K, value: V)

    fun delete(keyObj: K)

    fun getAll(): Map<K, V>

    fun get(keyObj: K): V?
}