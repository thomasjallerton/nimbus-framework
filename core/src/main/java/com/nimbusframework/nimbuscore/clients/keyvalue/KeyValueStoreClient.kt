package com.nimbusframework.nimbuscore.clients.keyvalue

import com.nimbusframework.nimbuscore.clients.store.ReadItemRequest
import com.nimbusframework.nimbuscore.clients.store.WriteItemRequest
import com.nimbusframework.nimbuscore.clients.store.conditions.Condition
import com.nimbusframework.nimbuscore.exceptions.NonRetryableException
import com.nimbusframework.nimbuscore.exceptions.RetryableException
import com.nimbusframework.nimbuscore.exceptions.StoreConditionException

interface KeyValueStoreClient<K, V> {
    @Throws(RetryableException::class, NonRetryableException::class)
    fun put(key: K, value: V)

    @Throws(StoreConditionException::class, RetryableException::class, NonRetryableException::class)
    fun put(key: K, value: V, condition: Condition)

    @Throws(RetryableException::class, NonRetryableException::class)
    fun delete(keyObj: K)

    @Throws(StoreConditionException::class, RetryableException::class, NonRetryableException::class)
    fun delete(keyObj: K, condition: Condition)

    @Throws(RetryableException::class, NonRetryableException::class)
    fun getAll(): Map<K, V>

    @Throws(RetryableException::class, NonRetryableException::class)
    fun get(keyObj: K): V?

    @Throws(RetryableException::class, NonRetryableException::class)
    fun filter(condition: Condition): List<V>

    fun getReadItem(keyObj: K): ReadItemRequest<V>

    fun getWriteItem(key: K, value: V): WriteItemRequest

    fun getWriteItem(key: K, value: V, condition: Condition): WriteItemRequest

    fun getIncrementValueRequest(key: K, numericFieldName: String, amount: Number): WriteItemRequest

    fun getIncrementValueRequest(key: K, numericFieldName: String, amount: Number, condition: Condition): WriteItemRequest

    fun getDecrementValueRequest(key: K, numericFieldName: String, amount: Number): WriteItemRequest

    fun getDecrementValueRequest(key: K, numericFieldName: String, amount: Number, condition: Condition): WriteItemRequest

    fun getDeleteItemRequest(key: K): WriteItemRequest

    fun getDeleteItemRequest(key: K, condition: Condition): WriteItemRequest
}
