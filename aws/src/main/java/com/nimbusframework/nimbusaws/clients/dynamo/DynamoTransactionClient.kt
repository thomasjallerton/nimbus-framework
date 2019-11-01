package com.nimbusframework.nimbusaws.clients.dynamo

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.model.TransactGetItemsRequest
import com.amazonaws.services.dynamodbv2.model.TransactWriteItemsRequest
import com.nimbusframework.nimbuscore.clients.store.ReadItemRequest
import com.nimbusframework.nimbuscore.clients.store.TransactionalClient
import com.nimbusframework.nimbuscore.clients.store.WriteItemRequest

class DynamoTransactionClient: TransactionalClient {

    private val client = AmazonDynamoDBClientBuilder.defaultClient()

    override fun executeWriteTransaction(request: List<WriteItemRequest>) {
        val dynamoRequests = request.map { item ->
            if (item is DynamoWriteTransactItemRequest) {
                item.transactWriteItem
            } else {
                throw IllegalStateException("Did not expect non-AWS write request")
            }
        }
        client.transactWriteItems(TransactWriteItemsRequest().withTransactItems(dynamoRequests))
    }

    override fun executeReadTransaction(request: List<ReadItemRequest<out Any>>): List<Any> {
        val dynamoRequests = request.map { item ->
            if (item is DynamoReadItemRequest) {
                item.transactReadItem
            } else {
                throw IllegalStateException("Did not expect non-AWS read request")
            }
        }
        val result = client.transactGetItems(TransactGetItemsRequest().withTransactItems(dynamoRequests))
        return result.responses.mapIndexed { index, item -> (request[index] as DynamoReadItemRequest).getItem(item) }
    }

}
