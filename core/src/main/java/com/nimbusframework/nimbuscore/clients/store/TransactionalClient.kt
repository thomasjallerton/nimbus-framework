package com.nimbusframework.nimbuscore.clients.store

interface TransactionalClient {

    fun executeWriteTransaction(request: List<WriteItemRequest>)

    fun executeReadTransaction(request: List<ReadItemRequest<out Any>>): List<Any>

}