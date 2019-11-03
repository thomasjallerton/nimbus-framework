package com.nimbusframework.nimbuscore.clients.document

import com.nimbusframework.nimbuscore.clients.store.ReadItemRequest
import com.nimbusframework.nimbuscore.clients.store.UpdateCondition
import com.nimbusframework.nimbuscore.clients.store.WriteItemRequest
import com.nimbusframework.nimbuscore.exceptions.StoreConditionException

interface DocumentStoreClient<T> {

    fun put(obj: T)

    fun delete(obj: T)

    fun deleteKey(keyObj: Any)

    fun getAll(): List<T>

    fun get(keyObj: Any): T?

    fun getReadItem(keyObj: Any): ReadItemRequest<T>

    fun getWriteItem(obj: T): WriteItemRequest

    fun getIncrementValueRequest(keyObj: Any, numericFieldName: String, amount: Number): WriteItemRequest

    fun getIncrementValueRequest(keyObj: Any, numericFieldName: String, amount: Number, updateCondition: UpdateCondition): WriteItemRequest

    fun getDecrementValueRequest(keyObj: Any, numericFieldName: String, amount: Number): WriteItemRequest

    fun getDecrementValueRequest(keyObj: Any, numericFieldName: String, amount: Number, updateCondition: UpdateCondition): WriteItemRequest

    fun getDeleteItemRequest(keyObj: Any): WriteItemRequest
}