package com.nimbusframework.nimbuslocal.deployment.store

import com.nimbusframework.nimbuscore.clients.store.WriteItemRequest

class WriteItemRequestLocal(
        val storeTransactions: LocalStoreTransactions,
        val executeWrite: () -> Unit): WriteItemRequest()