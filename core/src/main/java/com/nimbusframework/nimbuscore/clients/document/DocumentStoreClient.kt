package com.nimbusframework.nimbuscore.clients.document

import com.nimbusframework.nimbuscore.clients.store.ReadItemRequest
import com.nimbusframework.nimbuscore.clients.store.conditions.ComparisionCondition
import com.nimbusframework.nimbuscore.clients.store.WriteItemRequest
import com.nimbusframework.nimbuscore.clients.store.conditions.Condition

interface DocumentStoreClient<T> {

    fun put(obj: T)

    fun delete(obj: T)

    fun deleteKey(keyObj: Any)

    fun getAll(): List<T>

    fun get(keyObj: Any): T?

    fun getReadItem(keyObj: Any): ReadItemRequest<T>

    fun getWriteItem(obj: T): WriteItemRequest

    fun getIncrementValueRequest(keyObj: Any, numericFieldName: String, amount: Number): WriteItemRequest

    fun getIncrementValueRequest(keyObj: Any, numericFieldName: String, amount: Number, condition: Condition): WriteItemRequest

    fun getDecrementValueRequest(keyObj: Any, numericFieldName: String, amount: Number): WriteItemRequest

    fun getDecrementValueRequest(keyObj: Any, numericFieldName: String, amount: Number, condition: Condition): WriteItemRequest

    fun getDeleteItemRequest(keyObj: Any): WriteItemRequest
}