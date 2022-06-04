package com.nimbusframework.nimbuscore.clients.empty

import com.nimbusframework.nimbuscore.clients.document.DocumentStoreClient
import com.nimbusframework.nimbuscore.clients.store.ReadItemRequest
import com.nimbusframework.nimbuscore.clients.store.WriteItemRequest
import com.nimbusframework.nimbuscore.clients.store.conditions.Condition
import com.nimbusframework.nimbuscore.exceptions.PermissionException
import java.util.stream.Stream

internal class EmptyDocumentStoreClient<T>: DocumentStoreClient<T> {

    private val clientName = "DocumentStoreClient"

    override fun put(obj: T, condition: Condition) {
        throw PermissionException(clientName)
    }

    override fun delete(obj: T, condition: Condition) {
        throw PermissionException(clientName)
    }

    override fun deleteKey(keyObj: Any, condition: Condition) {
        throw PermissionException(clientName)
    }

    override fun getWriteItem(obj: T, condition: Condition): WriteItemRequest {
        throw PermissionException(clientName)
    }

    override fun getIncrementValueRequest(keyObj: Any, numericFieldName: String, amount: Number, condition: Condition): WriteItemRequest {
        throw PermissionException(clientName)
    }

    override fun getDecrementValueRequest(keyObj: Any, numericFieldName: String, amount: Number, condition: Condition): WriteItemRequest {
        throw PermissionException(clientName)
    }

    override fun put(obj: T) {
        throw PermissionException(clientName)
    }

    override fun delete(obj: T) {
        throw PermissionException(clientName)
    }

    override fun deleteKey(keyObj: Any) {
        throw PermissionException(clientName)
    }

    override fun getAll(): Stream<T> {
        throw PermissionException(clientName)
    }

    override fun get(keyObj: Any): T? {
        throw PermissionException(clientName)
    }

    override fun getReadItem(keyObj: Any): ReadItemRequest<T> {
        throw PermissionException(clientName)
    }

    override fun getWriteItem(obj: T): WriteItemRequest {
        throw PermissionException(clientName)
    }

    override fun getIncrementValueRequest(keyObj: Any, numericFieldName: String, amount: Number): WriteItemRequest {
        throw PermissionException(clientName)
    }

    override fun getDecrementValueRequest(keyObj: Any, numericFieldName: String, amount: Number): WriteItemRequest {
        throw PermissionException(clientName)
    }

    override fun getDeleteKeyItemRequest(keyObj: Any): WriteItemRequest {
        throw PermissionException(clientName)
    }

    override fun getDeleteKeyItemRequest(keyObj: Any, condition: Condition): WriteItemRequest {
        throw PermissionException(clientName)
    }

    override fun getDeleteItemRequest(obj: T): WriteItemRequest {
        throw PermissionException(clientName)
    }

    override fun getDeleteItemRequest(obj: T, condition: Condition): WriteItemRequest {
        throw PermissionException(clientName)
    }

    override fun filter(condition: Condition): Stream<T> {
        throw PermissionException(clientName)
    }

    override fun getAllKeys(): Stream<Any> {
        throw PermissionException(clientName)
    }
}
