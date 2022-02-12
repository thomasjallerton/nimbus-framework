package com.nimbusframework.nimbuslocal.clients

import com.nimbusframework.nimbuscore.clients.document.DocumentStoreClient
import com.nimbusframework.nimbuscore.clients.store.ReadItemRequest
import com.nimbusframework.nimbuscore.clients.store.WriteItemRequest
import com.nimbusframework.nimbuscore.clients.store.conditions.Condition
import com.nimbusframework.nimbuscore.permissions.PermissionType

class DocumentStoreClientLocal<T>(private val clazz: Class<T>) : DocumentStoreClient<T>, LocalClient() {

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

    override fun delete(obj: T, condition: Condition) {
        documentStore.delete(obj, condition)
    }

    override fun deleteKey(keyObj: Any, condition: Condition) {
        documentStore.deleteKey(keyObj, condition)
    }

    override fun getDecrementValueRequest(keyObj: Any, numericFieldName: String, amount: Number): WriteItemRequest {
        return documentStore.getDecrementValueRequest(keyObj, numericFieldName, amount)
    }

    override fun getDecrementValueRequest(keyObj: Any, numericFieldName: String, amount: Number, condition: Condition): WriteItemRequest {
        return documentStore.getDecrementValueRequest(keyObj, numericFieldName, amount, condition)
    }

    override fun getIncrementValueRequest(keyObj: Any, numericFieldName: String, amount: Number): WriteItemRequest {
        return documentStore.getIncrementValueRequest(keyObj, numericFieldName, amount)
    }

    override fun getIncrementValueRequest(keyObj: Any, numericFieldName: String, amount: Number, condition: Condition): WriteItemRequest {
        return documentStore.getIncrementValueRequest(keyObj, numericFieldName, amount, condition)
    }

    override fun getReadItem(keyObj: Any): ReadItemRequest<T> {
        return documentStore.getReadItem(keyObj)
    }

    override fun getWriteItem(obj: T): WriteItemRequest {
        return documentStore.getWriteItem(obj)
    }

    override fun getWriteItem(obj: T, condition: Condition): WriteItemRequest {
        return documentStore.getWriteItem(obj, condition)
    }

    override fun put(obj: T, condition: Condition) {
        return documentStore.put(obj, condition)
    }

    override fun getDeleteItemRequest(obj: T): WriteItemRequest {
        return documentStore.getDeleteItemRequest(obj)
    }

    override fun getDeleteItemRequest(obj: T, condition: Condition): WriteItemRequest {
        return documentStore.getDeleteItemRequest(obj, condition)
    }

    override fun getDeleteKeyItemRequest(keyObj: Any): WriteItemRequest {
        return documentStore.getDeleteKeyItemRequest(keyObj)
    }

    override fun getDeleteKeyItemRequest(keyObj: Any, condition: Condition): WriteItemRequest {
        return documentStore.getDeleteKeyItemRequest(keyObj, condition)
    }

}