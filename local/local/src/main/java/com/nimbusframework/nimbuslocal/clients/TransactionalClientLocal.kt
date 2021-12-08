package com.nimbusframework.nimbuslocal.clients

import com.nimbusframework.nimbuscore.clients.store.ReadItemRequest
import com.nimbusframework.nimbuscore.clients.store.TransactionalClient
import com.nimbusframework.nimbuscore.clients.store.WriteItemRequest
import com.nimbusframework.nimbuscore.exceptions.NonRetryableException
import com.nimbusframework.nimbuscore.exceptions.StoreConditionException
import com.nimbusframework.nimbuslocal.deployment.store.ReadItemRequestLocal
import com.nimbusframework.nimbuslocal.deployment.store.WriteItemRequestLocal
import java.lang.IllegalStateException
import java.util.*

class TransactionalClientLocal : TransactionalClient {

    override fun executeReadTransaction(requests: List<ReadItemRequest<out Any>>): List<Any?> {
        return requests.map { request ->
            if (request is ReadItemRequestLocal) {
                request.executeRead()
            } else {
                throw IllegalStateException("Did not expect non-local read request")
            }
        }
    }

    override fun executeWriteTransaction(requests: List<WriteItemRequest>) {
        val transactionUid = UUID.randomUUID()
        val stores = requests.map {
            if (it is WriteItemRequestLocal) {
                it.storeTransactions
            } else {
                throw IllegalStateException("Did not expect non-local read request")
            }
        }

        try {
            requests.forEach { request ->
                request as WriteItemRequestLocal
                request.storeTransactions.startTransaction(transactionUid)
                request.executeWrite()
            }
            stores.forEach { it.successfulTransaction(transactionUid) }
        } catch (e: StoreConditionException) {
            stores.forEach { it.failedTransaction(transactionUid) }
            throw StoreConditionException()
        } catch (e: Exception) {
            stores.forEach { it.failedTransaction(transactionUid) }
            throw NonRetryableException(e.localizedMessage)
        }
    }
}