package com.nimbusframework.nimbuscore.clients.store

import com.nimbusframework.nimbuscore.exceptions.NonRetryableException
import com.nimbusframework.nimbuscore.exceptions.RetryableException
import com.nimbusframework.nimbuscore.exceptions.StoreConditionException

interface TransactionalClient {

    @Throws(StoreConditionException::class, RetryableException::class, NonRetryableException::class)
    fun executeWriteTransaction(request: List<WriteItemRequest>)

    fun executeReadTransaction(request: List<ReadItemRequest<out Any>>): List<Any>

}