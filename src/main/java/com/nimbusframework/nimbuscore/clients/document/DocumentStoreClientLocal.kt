package com.nimbusframework.nimbuscore.clients.document

import com.nimbusframework.nimbuscore.clients.LocalClient
import com.nimbusframework.nimbuscore.clients.PermissionException
import com.nimbusframework.nimbuscore.testing.function.PermissionType

class DocumentStoreClientLocal<T>(private val clazz: Class<T>, stage: String) : DocumentStoreClient<T>, LocalClient() {

    private val documentStore = localNimbusDeployment.getDocumentStore(clazz)

    override fun canUse(): Boolean {
        return checkPermissions(PermissionType.DOCUMENT_STORE, clazz.canonicalName)
    }

    override val clientName: String = DocumentStoreClient::class.java.simpleName

    override fun put(obj: T) {
        checkClientUse()
        documentStore.put(obj)
    }

    override fun delete(obj: T) {
        checkClientUse()
        documentStore.delete(obj)
    }

    override fun deleteKey(keyObj: Any) {
        checkClientUse()
        documentStore.deleteKey(keyObj)
    }

    override fun getAll(): List<T> {
        checkClientUse()
        return documentStore.getAll()
    }

    override fun get(keyObj: Any): T? {
        checkClientUse()
        return documentStore.get(keyObj)
    }
}