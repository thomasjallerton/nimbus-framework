package com.nimbusframework.nimbusaws.clients.dynamo

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.model.*
import com.nimbusframework.nimbuscore.clients.store.ReadItemRequest
import com.nimbusframework.nimbuscore.clients.store.TransactionalClient
import com.nimbusframework.nimbuscore.clients.store.WriteItemRequest
import com.nimbusframework.nimbuscore.exceptions.NonRetryableException
import com.nimbusframework.nimbuscore.exceptions.RetryableException
import com.nimbusframework.nimbuscore.exceptions.StoreConditionException

class DynamoTransactionClient: TransactionalClient {

    private val client = AmazonDynamoDBClientBuilder.defaultClient()

    override fun executeWriteTransaction(requests: List<WriteItemRequest>) {
        val dynamoRequests = requests.map { item ->
            if (item is DynamoWriteTransactItemRequest) {
                item.transactWriteItem
            } else {
                throw IllegalStateException("Did not expect non-AWS write request")
            }
        }

        executeTransaction { client.transactWriteItems(TransactWriteItemsRequest().withTransactItems(dynamoRequests)) }
    }

    override fun executeReadTransaction(requests: List<ReadItemRequest<out Any>>): List<Any?> {

        val dynamoRequests = requests.map { item ->
            if (item is DynamoReadItemRequest) {
                item.transactReadItem
            } else {
                throw IllegalStateException("Did not expect non-AWS read request")
            }
        }
        val result = executeTransaction { client.transactGetItems(TransactGetItemsRequest().withTransactItems(dynamoRequests)) }
        return result.responses.mapIndexed { index, item -> (requests[index] as DynamoReadItemRequest).getItem(item) }
    }

    private fun <T> executeTransaction(toExecute: () -> T): T {
        try {
            return toExecute()
        } catch (e: TransactionCanceledException) {
            for (reason in e.cancellationReasons) {
                if (reason.code == "ConditionalCheckFailed") {
                    throw StoreConditionException()
                }
            }
            if (e.isRetryable) {
                throw RetryableException(e.localizedMessage)
            } else {
                throw NonRetryableException(e.localizedMessage)
            }
        }
    }
}
