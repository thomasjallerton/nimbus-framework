package com.nimbusframework.nimbuscore.clients.empty

import com.nimbusframework.nimbuscore.clients.document.DocumentStoreClient
import com.nimbusframework.nimbuscore.clients.store.ReadItemRequest
import com.nimbusframework.nimbuscore.clients.store.UpdateCondition
import com.nimbusframework.nimbuscore.clients.store.WriteItemRequest
import com.nimbusframework.nimbuscore.exceptions.PermissionException

internal class EmptyDocumentStoreClient<T>: DocumentStoreClient<T> {

    private val clientName = "DocumentStoreClient"
    override fun put(obj: T) {
        throw PermissionException(clientName)
    }

    override fun delete(obj: T) {
        throw PermissionException(clientName)
    }

    override fun deleteKey(keyObj: Any) {
        throw PermissionException(clientName)
    }

    override fun getAll(): List<T> {
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

    override fun getDeleteItemRequest(keyObj: Any): WriteItemRequest {
        throw PermissionException(clientName)
    }

    override fun getIncrementValueRequest(keyObj: Any, numericFieldName: String, amount: Number, updateCondition: UpdateCondition): WriteItemRequest {
        throw PermissionException(clientName)
    }

    override fun getDecrementValueRequest(keyObj: Any, numericFieldName: String, amount: Number, updateCondition: UpdateCondition): WriteItemRequest {
        throw PermissionException(clientName)
    }

}