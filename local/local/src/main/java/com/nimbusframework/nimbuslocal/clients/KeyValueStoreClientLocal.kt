package com.nimbusframework.nimbuslocal.clients

import com.nimbusframework.nimbuscore.clients.keyvalue.KeyValueStoreClient
import com.nimbusframework.nimbuscore.clients.store.ReadItemRequest
import com.nimbusframework.nimbuscore.clients.store.WriteItemRequest
import com.nimbusframework.nimbuscore.clients.store.conditions.Condition
import com.nimbusframework.nimbuscore.permissions.PermissionType
import com.nimbusframework.nimbuslocal.deployment.keyvalue.LocalKeyValueStore

internal class KeyValueStoreClientLocal<K, V>(
        private val valueClass: Class<V>
): KeyValueStoreClient<K, V>, LocalClient(PermissionType.KEY_VALUE_STORE) {

    private val table: LocalKeyValueStore<K, V> = localNimbusDeployment.getKeyValueStore(valueClass)

    override fun canUse(permissionType: PermissionType): Boolean {
        return checkPermissions(permissionType, valueClass.canonicalName)
    }
    override val clientName: String = KeyValueStoreClientLocal::class.java.simpleName

    override fun put(key: K, value: V) {
        checkClientUse()
        table.put(key, value)
    }

    override fun delete(keyObj: K) {
        checkClientUse()
        table.delete(keyObj)
    }

    override fun getAll(): Map<K, V> {
        checkClientUse()
        return table.getAll()
    }

    override fun get(keyObj: K): V? {
        checkClientUse()
        return table.get(keyObj)
    }

    override fun delete(keyObj: K, condition: Condition) {
        return table.delete(keyObj, condition)
    }

    override fun getDecrementValueRequest(key: K, numericFieldName: String, amount: Number): WriteItemRequest {
        return table.getDecrementValueRequest(key, numericFieldName, amount)
    }

    override fun getDecrementValueRequest(key: K, numericFieldName: String, amount: Number, condition: Condition): WriteItemRequest {
        return table.getDecrementValueRequest(key, numericFieldName, amount, condition)
    }

    override fun getDeleteItemRequest(key: K): WriteItemRequest {
        return table.getDeleteItemRequest(key)
    }

    override fun getDeleteItemRequest(key: K, condition: Condition): WriteItemRequest {
        return table.getDeleteItemRequest(key, condition)
    }

    override fun getIncrementValueRequest(key: K, numericFieldName: String, amount: Number): WriteItemRequest {
        return table.getIncrementValueRequest(key, numericFieldName, amount)
    }

    override fun getIncrementValueRequest(key: K, numericFieldName: String, amount: Number, condition: Condition): WriteItemRequest {
        return table.getIncrementValueRequest(key, numericFieldName, amount, condition)
    }

    override fun getReadItem(keyObj: K): ReadItemRequest<V> {
        return table.getReadItem(keyObj)
    }

    override fun getWriteItem(key: K, value: V): WriteItemRequest {
        return table.getWriteItem(key, value)
    }

    override fun getWriteItem(key: K, value: V, condition: Condition): WriteItemRequest {
        return table.getWriteItem(key, value, condition)
    }

    override fun put(key: K, value: V, condition: Condition) {
        return table.put(key, value, condition)
    }

    override fun filter(condition: Condition): List<V> {
        return table.filter(condition)
    }
}
