package com.nimbusframework.nimbuscore.clients.keyvalue

import com.nimbusframework.nimbuscore.clients.PermissionException

class EmptyKeyValueStoreClient<K, V>: KeyValueStoreClient<K, V> {
    private val clientName = "KeyValueStoreClient"

    override fun put(key: K, value: V) {
        throw PermissionException(clientName)
    }

    override fun delete(keyObj: K) {
        throw PermissionException(clientName)
    }

    override fun getAll(): Map<K, V> {
        throw PermissionException(clientName)
    }

    override fun get(keyObj: K): V? {
        throw PermissionException(clientName)
    }
}