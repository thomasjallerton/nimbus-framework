package com.nimbusframework.nimbusaws.clients.dynamo

import com.amazonaws.services.dynamodbv2.model.TransactWriteItem
import com.nimbusframework.nimbuscore.clients.store.WriteItemRequest

class DynamoWriteTransactItemRequest(
        val transactWriteItem: TransactWriteItem
): WriteItemRequest()