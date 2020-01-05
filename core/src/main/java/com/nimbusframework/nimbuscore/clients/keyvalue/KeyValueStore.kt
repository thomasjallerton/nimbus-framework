package com.nimbusframework.nimbuscore.clients.keyvalue

import com.nimbusframework.nimbuscore.clients.ClientBuilder

open class KeyValueStore<K, V>(keyClass: Class<K>, valueClass: Class<V>) {

    private val keyValueStoreClient = ClientBuilder.getKeyValueStoreClient(keyClass, valueClass)

    fun getClient(): KeyValueStoreClient<K, V> {
        return keyValueStoreClient
    }

}