package com.nimbusframework.nimbuscore.clients.document

import com.nimbusframework.nimbuscore.clients.store.ReadItemRequest
import com.nimbusframework.nimbuscore.clients.store.WriteItemRequest
import com.nimbusframework.nimbuscore.clients.store.conditions.Condition
import com.nimbusframework.nimbuscore.exceptions.NonRetryableException
import com.nimbusframework.nimbuscore.exceptions.RetryableException
import com.nimbusframework.nimbuscore.exceptions.StoreConditionException
import java.util.stream.Stream

interface DocumentStoreClient<T> {

    @Throws(RetryableException::class, NonRetryableException::class)
    fun put(obj: T)

    @Throws(StoreConditionException::class, RetryableException::class, NonRetryableException::class)
    fun put(obj: T, condition: Condition)

    @Throws(RetryableException::class, NonRetryableException::class)
    fun delete(obj: T)

    @Throws(StoreConditionException::class, RetryableException::class, NonRetryableException::class)
    fun delete(obj: T, condition: Condition)

    fun deleteKey(keyObj: Any)

    @Throws(StoreConditionException::class, RetryableException::class, NonRetryableException::class)
    fun deleteKey(keyObj: Any, condition: Condition)

    @Throws(RetryableException::class, NonRetryableException::class)
    fun getAll(): Stream<T>

    @Throws(RetryableException::class, NonRetryableException::class)
    fun getAllKeys(): Stream<Any>

    @Throws(RetryableException::class, NonRetryableException::class)
    fun get(keyObj: Any): T?

    @Throws(RetryableException::class, NonRetryableException::class)
    fun filter(condition: Condition): Stream<T>

    fun getReadItem(keyObj: Any): ReadItemRequest<T>

    fun getWriteItem(obj: T): WriteItemRequest

    fun getWriteItem(obj: T, condition: Condition): WriteItemRequest

    fun getIncrementValueRequest(keyObj: Any, numericFieldName: String, amount: Number): WriteItemRequest

    fun getIncrementValueRequest(keyObj: Any, numericFieldName: String, amount: Number, condition: Condition): WriteItemRequest

    fun getDecrementValueRequest(keyObj: Any, numericFieldName: String, amount: Number): WriteItemRequest

    fun getDecrementValueRequest(keyObj: Any, numericFieldName: String, amount: Number, condition: Condition): WriteItemRequest

    fun getDeleteKeyItemRequest(keyObj: Any): WriteItemRequest

    fun getDeleteKeyItemRequest(keyObj: Any, condition: Condition): WriteItemRequest

    fun getDeleteItemRequest(obj: T): WriteItemRequest

    fun getDeleteItemRequest(obj: T, condition: Condition): WriteItemRequest
}
