package com.nimbusframework.nimbusaws.clients.dynamo

import com.nimbusframework.nimbuscore.clients.store.WriteItemRequest
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItem

class DynamoWriteTransactItemRequest(
        val transactWriteItem: TransactWriteItem
): WriteItemRequest()
