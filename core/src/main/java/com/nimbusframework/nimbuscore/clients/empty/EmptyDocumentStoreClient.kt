package com.nimbusframework.nimbuscore.clients.empty

import com.nimbusframework.nimbuscore.clients.document.DocumentStoreClient
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

}