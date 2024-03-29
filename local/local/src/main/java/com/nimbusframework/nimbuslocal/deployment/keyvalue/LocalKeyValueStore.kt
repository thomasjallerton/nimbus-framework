package com.nimbusframework.nimbuslocal.deployment.keyvalue

import com.nimbusframework.nimbuscore.clients.JacksonClient
import com.nimbusframework.nimbuscore.clients.keyvalue.AbstractKeyValueStoreClient
import com.nimbusframework.nimbuscore.clients.store.ReadItemRequest
import com.nimbusframework.nimbuscore.clients.store.WriteItemRequest
import com.nimbusframework.nimbuscore.clients.store.conditions.Condition
import com.nimbusframework.nimbuslocal.deployment.store.LocalStore
import com.nimbusframework.nimbuslocal.deployment.store.LocalStoreTransactions
import java.util.*
import java.util.stream.Stream

class LocalKeyValueStore<K, V>(private val keyClass: Class<K>, private val valueClass: Class<V>, keyType: Class<*>, keyName: String, tableName: String, stage: String)
    : AbstractKeyValueStoreClient<K, V>(keyClass, valueClass, keyType, keyName, tableName, stage), LocalStoreTransactions {

    private var keyValueStore = LocalStore(keyClass, valueClass, keyName, attributes)

    internal val internalTableName = userTableName

    override fun put(key: K, value: V) {
        keyValueStore.put(key, value)
    }

    override fun delete(keyObj: K) {
        keyValueStore.delete(keyObj)
    }

    internal fun putJson(obj: String) {
        val root = JacksonClient.readTree(obj)
        val key = JacksonClient.readValue(root[keyName].toString(), keyClass)
        val objVal = JacksonClient.readValue(obj, valueClass)

        put(key, objVal)
    }

    internal fun deleteJson(obj: String) {
        val root = JacksonClient.readTree(obj)
        val key = JacksonClient.readValue(root[keyName].toString(), keyClass)

        delete(key)
    }

    override fun get(keyObj: K): V? {
        return keyValueStore.get(keyObj)
    }

    override fun delete(keyObj: K, condition: Condition) {
        keyValueStore.delete(keyObj, condition)
    }

    override fun getDecrementValueRequest(key: K, numericFieldName: String, amount: Number): WriteItemRequest {
        return keyValueStore.getDecrementValueRequest(key, numericFieldName, amount)
    }

    override fun getDecrementValueRequest(key: K, numericFieldName: String, amount: Number, condition: Condition): WriteItemRequest {
        return keyValueStore.getDecrementValueRequest(key, numericFieldName, amount, condition)
    }

    override fun getDeleteItemRequest(key: K): WriteItemRequest {
        return keyValueStore.getDeleteKeyItemRequest(key)
    }

    override fun getDeleteItemRequest(key: K, condition: Condition): WriteItemRequest {
        return keyValueStore.getDeleteKeyItemRequest(key, condition)
    }

    override fun getIncrementValueRequest(key: K, numericFieldName: String, amount: Number): WriteItemRequest {
        return keyValueStore.getIncrementValueRequest(key, numericFieldName, amount)
    }

    override fun getIncrementValueRequest(key: K, numericFieldName: String, amount: Number, condition: Condition): WriteItemRequest {
        return keyValueStore.getIncrementValueRequest(key, numericFieldName, amount, condition)
    }

    override fun getReadItem(keyObj: K): ReadItemRequest<V> {
        return keyValueStore.getReadItem(keyObj)
    }

    override fun getWriteItem(key: K, value: V): WriteItemRequest {
        return keyValueStore.getWriteItem(key, value)
    }

    override fun getWriteItem(key: K, value: V, condition: Condition): WriteItemRequest {
        return keyValueStore.getWriteItem(key, value, condition)
    }

    override fun put(key: K, value: V, condition: Condition) {
        keyValueStore.put(key, value, condition)
    }

    fun size(): Int {
        return keyValueStore.size()
    }

    internal fun addMethod(method: KeyValueMethod) {
        keyValueStore.addMethod(method)
    }


    override fun getAll(): Map<K, V> {
        return keyValueStore.getAll()
    }

    override fun startTransaction(transactionUid: UUID) {
        keyValueStore.startTransaction(transactionUid)
    }

    override fun successfulTransaction(transactionUid: UUID) {
        keyValueStore.successfulTransaction(transactionUid)
    }

    override fun failedTransaction(transactionUid: UUID) {
        keyValueStore.failedTransaction(transactionUid)
    }

    override fun filter(condition: Condition): Map<K, V> {
        return keyValueStore.filter(condition)
    }

    override fun getAllKeys(): Stream<K> {
        return keyValueStore.getAllKeys().stream()
    }

}
