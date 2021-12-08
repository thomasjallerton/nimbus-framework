package com.nimbusframework.nimbuslocal.deployment.store

import java.util.*

interface LocalStoreTransactions {
    fun startTransaction(transactionUid: UUID)
    fun successfulTransaction(transactionUid: UUID)
    fun failedTransaction(transactionUid: UUID)
}