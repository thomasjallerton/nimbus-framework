package com.nimbusframework.nimbuscore.clients.document

import com.nimbusframework.nimbuscore.clients.ClientBuilder
import com.nimbusframework.nimbuscore.clients.store.ReadItemRequest
import com.nimbusframework.nimbuscore.clients.store.WriteItemRequest
import com.nimbusframework.nimbuscore.clients.store.conditions.Condition

open class DocumentStore<T>(documentStore: Class<T>) {

    private val documentStoreClient = ClientBuilder.getDocumentStoreClient(documentStore)

    fun getClient(): DocumentStoreClient<T> {
        return documentStoreClient
    }

}