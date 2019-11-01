package com.nimbusframework.nimbuscore.clients.document

import com.nimbusframework.nimbuscore.clients.store.ReadItemRequest
import com.nimbusframework.nimbuscore.clients.store.WriteItemRequest

interface DocumentStoreClient<T> {

    fun put(obj: T)

    fun delete(obj: T)

    fun deleteKey(keyObj: Any)

    fun getAll(): List<T>

    fun get(keyObj: Any): T?

    fun getReadItem(keyObj: Any): ReadItemRequest<T>

    fun getWriteItem(obj: T): WriteItemRequest
}