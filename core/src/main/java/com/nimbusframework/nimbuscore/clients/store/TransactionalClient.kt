package com.nimbusframework.nimbuscore.clients.store

import com.nimbusframework.nimbuscore.exceptions.NonRetryableException
import com.nimbusframework.nimbuscore.exceptions.RetryableException
import com.nimbusframework.nimbuscore.exceptions.StoreConditionException

interface TransactionalClient {

    @Throws(StoreConditionException::class, RetryableException::class, NonRetryableException::class)
    fun executeWriteTransaction(requests: List<WriteItemRequest>)

    @Throws(RetryableException::class, NonRetryableException::class)
    fun executeReadTransaction(requests: List<ReadItemRequest<out Any>>): List<Any?>

}