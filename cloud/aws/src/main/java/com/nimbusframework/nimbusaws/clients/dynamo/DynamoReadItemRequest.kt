package com.nimbusframework.nimbusaws.clients.dynamo

import com.nimbusframework.nimbuscore.clients.store.ReadItemRequest
import software.amazon.awssdk.services.dynamodb.model.ItemResponse
import software.amazon.awssdk.services.dynamodb.model.TransactGetItem

class DynamoReadItemRequest<T>(
        val transactReadItem: TransactGetItem,
        val getItem: (ItemResponse) -> T?
): ReadItemRequest<T>()
