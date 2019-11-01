package com.nimbusframework.nimbuscore.clients.empty

import com.nimbusframework.nimbuscore.clients.store.ReadItemRequest
import com.nimbusframework.nimbuscore.clients.store.TransactionalClient
import com.nimbusframework.nimbuscore.clients.store.WriteItemRequest
import com.nimbusframework.nimbuscore.exceptions.PermissionException

class EmptyTransactionalClient: TransactionalClient {

    private val clientName = "TransactionalClient"

    override fun executeWriteTransaction(request: List<WriteItemRequest>) {
        throw PermissionException(clientName)
    }

    override fun executeReadTransaction(request: List<ReadItemRequest<out Any>>): List<Any> {
        throw PermissionException(clientName)
    }

}