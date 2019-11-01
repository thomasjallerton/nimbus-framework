package com.nimbusframework.nimbuscore.clients.keyvalue

import com.nimbusframework.nimbuscore.clients.store.ReadItemRequest
import com.nimbusframework.nimbuscore.clients.store.WriteItemRequest

interface KeyValueStoreClient<K, V> {
    fun put(key: K, value: V)

    fun delete(keyObj: K)

    fun getAll(): Map<K, V>

    fun get(keyObj: K): V?

    fun getReadItem(keyObj: K): ReadItemRequest<V>

    fun getWriteItem(key: K, value: V): WriteItemRequest
}