package com.nimbusframework.nimbuscore.clients.document

import com.nimbusframework.nimbuscore.testing.LocalNimbusDeployment

class DocumentStoreClientLocal<T>(private val clazz: Class<T>, stage: String): DocumentStoreClient<T>(clazz, stage) {

    private val localNimbusDeployment = LocalNimbusDeployment.getInstance()
    private val documentStore = localNimbusDeployment.getDocumentStore(clazz)

    override fun put(obj: T) {
        documentStore.put(obj)
    }

    override fun delete(obj: T) {
        documentStore.delete(obj)
    }

    override fun deleteKey(keyObj: Any) {
        documentStore.deleteKey(keyObj)
    }

    override fun getAll(): List<T> {
        return documentStore.getAll()
    }

    override fun get(keyObj: Any): T? {
        return documentStore.get(keyObj)
    }
}