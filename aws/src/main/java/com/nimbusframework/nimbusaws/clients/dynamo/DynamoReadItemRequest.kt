package com.nimbusframework.nimbusaws.clients.dynamo

import com.amazonaws.services.dynamodbv2.model.ItemResponse
import com.amazonaws.services.dynamodbv2.model.TransactGetItem
import com.nimbusframework.nimbuscore.clients.store.ReadItemRequest

class DynamoReadItemRequest<T>(
        val transactReadItem: TransactGetItem,
        val getItem: (ItemResponse) -> T
): ReadItemRequest<T>()