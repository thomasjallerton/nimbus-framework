package com.nimbusframework.nimbuscore.clients.empty

import com.nimbusframework.nimbuscore.exceptions.PermissionException
import com.nimbusframework.nimbuscore.clients.keyvalue.KeyValueStoreClient
import com.nimbusframework.nimbuscore.clients.store.ReadItemRequest
import com.nimbusframework.nimbuscore.clients.store.UpdateCondition
import com.nimbusframework.nimbuscore.clients.store.WriteItemRequest

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

    override fun getReadItem(keyObj: K): ReadItemRequest<V> {
        throw PermissionException(clientName)
    }

    override fun getWriteItem(key: K, value: V): WriteItemRequest {
        throw PermissionException(clientName)
    }

    override fun getIncrementValueRequest(key: K, numericFieldName: String, amount: Number): WriteItemRequest {
        throw PermissionException(clientName)
    }

    override fun getDecrementValueRequest(key: K, numericFieldName: String, amount: Number): WriteItemRequest {
        throw PermissionException(clientName)
    }

    override fun getDeleteItemRequest(key: K): WriteItemRequest {
        throw PermissionException(clientName)
    }

    override fun getIncrementValueRequest(key: K, numericFieldName: String, amount: Number, updateCondition: UpdateCondition): WriteItemRequest {
        throw PermissionException(clientName)
    }

    override fun getDecrementValueRequest(key: K, numericFieldName: String, amount: Number, updateCondition: UpdateCondition): WriteItemRequest {
        throw PermissionException(clientName)
    }
}