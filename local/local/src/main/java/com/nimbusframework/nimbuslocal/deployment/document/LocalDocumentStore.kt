package com.nimbusframework.nimbuslocal.deployment.document

import com.nimbusframework.nimbuscore.clients.JacksonClient
import com.nimbusframework.nimbuscore.clients.document.AbstractDocumentStoreClient
import com.nimbusframework.nimbuscore.clients.store.ReadItemRequest
import com.nimbusframework.nimbuscore.clients.store.WriteItemRequest
import com.nimbusframework.nimbuscore.clients.store.conditions.Condition
import com.nimbusframework.nimbuslocal.deployment.store.LocalStore
import com.nimbusframework.nimbuslocal.deployment.store.LocalStoreTransactions
import java.util.*

class LocalDocumentStore<T>(private val clazz: Class<T>, tableName: String, stage: String)
    : AbstractDocumentStoreClient<T>(clazz, tableName, stage), LocalStoreTransactions {

    private var documentStore = LocalStore(Any::class.java, clazz, keys.keys.first(), allAttributes)

    internal val internalTableName = userTableName

    internal fun addMethod(method: DocumentMethod) {
        documentStore.addMethod(method)
    }

    fun size(): Int {
        return documentStore.size()
    }

    override fun delete(obj: T, condition: Condition) {
        documentStore.delete(getKeyValue(obj), condition)
    }

    override fun deleteKey(keyObj: Any, condition: Condition) {
        documentStore.delete(keyObj, condition)
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

    override fun getDeleteKeyItemRequest(keyObj: Any): WriteItemRequest {
        return documentStore.getDeleteKeyItemRequest(keyObj)
    }

    override fun getDeleteKeyItemRequest(keyObj: Any, condition: Condition): WriteItemRequest {
        return documentStore.getDeleteKeyItemRequest(keyObj, condition)
    }

    override fun getDeleteItemRequest(obj: T): WriteItemRequest {
        return documentStore.getDeleteKeyItemRequest(getKeyValue(obj))
    }

    override fun getDeleteItemRequest(obj: T, condition: Condition): WriteItemRequest {
        return documentStore.getDeleteKeyItemRequest(getKeyValue(obj), condition)
    }

    override fun getReadItem(keyObj: Any): ReadItemRequest<T> {
        return documentStore.getReadItem(keyObj)
    }

    override fun getWriteItem(obj: T): WriteItemRequest {
        return documentStore.getWriteItem(getKeyValue(obj), obj)
    }

    override fun getWriteItem(obj: T, condition: Condition): WriteItemRequest {
        return documentStore.getWriteItem(getKeyValue(obj), obj, condition)
    }

    override fun put(obj: T, condition: Condition) {
        return documentStore.put(getKeyValue(obj), obj, condition)
    }

    override fun put(obj: T) {
        return documentStore.put(getKeyValue(obj), obj)
    }

    internal fun putJson(obj: String) {
        put(JacksonClient.readValue(obj, clazz))
    }

    internal fun deleteJson(obj: String) {
        delete(JacksonClient.readValue(obj, clazz))
    }

    override fun delete(obj: T) {
        documentStore.delete(getKeyValue(obj))
    }

    override fun deleteKey(keyObj: Any) {
        documentStore.delete(keyObj)
    }

    override fun getAll(): List<T> {
        return documentStore.getAll().values.toList()
    }

    override fun get(keyObj: Any): T? {
        return documentStore.get(keyObj)
    }

    private fun getKeyValue(obj: T): Any {
        if (keys.size > 1) {
            throw Exception("Composite key shouldn't exist!!")
        } else if (keys.isEmpty()) {
            throw Exception("Need a key field!")
        }

        for ((_, field) in keys) {
            field.isAccessible = true
            return field[obj]
        }
        //should never hit here
        return Any()
    }


    override fun startTransaction(transactionUid: UUID) {
        documentStore.startTransaction(transactionUid)
    }

    override fun successfulTransaction(transactionUid: UUID) {
        documentStore.successfulTransaction(transactionUid)
    }

    override fun failedTransaction(transactionUid: UUID) {
        documentStore.failedTransaction(transactionUid)
    }

    override fun filter(condition: Condition): List<T> {
        return documentStore.filter(condition).values.toList()
    }
}
