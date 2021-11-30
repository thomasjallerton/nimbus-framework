package com.nimbusframework.nimbusaws.clients.dynamo

import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.nimbusframework.nimbuscore.exceptions.NonRetryableException
import com.nimbusframework.nimbuscore.exceptions.RetryableException
import com.nimbusframework.nimbuscore.exceptions.StoreConditionException
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.collections.shouldContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.*

class DynamoTransactionClientTest : AnnotationSpec() {

    private lateinit var underTest: DynamoTransactionClient
    private lateinit var mockDynamoDb: DynamoDbClient

    @BeforeEach
    fun setup() {
        mockDynamoDb = mockk(relaxed = true)
        underTest = DynamoTransactionClient(mockDynamoDb)
    }

    @Test
    fun canExecuteDynamoWriteTransactions() {
        val transactWriteItem: TransactWriteItem = mockk(relaxed = true)
        val writes = listOf(DynamoWriteTransactItemRequest(transactWriteItem))

        val request = slot<TransactWriteItemsRequest>()

        every { mockDynamoDb.transactWriteItems(capture(request))} returns null

        underTest.executeWriteTransaction(writes)

        request.captured.transactItems() shouldContain transactWriteItem
    }

    @Test
    fun canExecuteDynamoGetTransactions() {
        val transactGetItem: TransactGetItem = mockk(relaxed = true)
        val reads = listOf(DynamoReadItemRequest(transactGetItem) {"Test"})

        val request = slot<TransactGetItemsRequest>()

        every { mockDynamoDb.transactGetItems(capture(request))} returns TransactGetItemsResponse.builder().responses(ItemResponse.builder().item(mapOf()).build()).build()

        underTest.executeReadTransaction(reads) shouldContain "Test"

        request.captured.transactItems() shouldContain transactGetItem
    }

    @Test(expected = RetryableException::class)
    fun canHandleRetryableException() {
        val transactWriteItem: TransactWriteItem = mockk(relaxed = true)
        val writes = listOf(DynamoWriteTransactItemRequest(transactWriteItem))

        val exception = mockk<TransactionCanceledException>()
        every { exception.cancellationReasons() } returns listOf()
        every { exception.localizedMessage } returns ""
        every { exception.retryable() } returns true

        every { mockDynamoDb.transactWriteItems(any<TransactWriteItemsRequest>())} throws exception

        underTest.executeWriteTransaction(writes)
    }

    @Test(expected = NonRetryableException::class)
    fun canHandleNonRetryableException() {
        val transactWriteItem: TransactWriteItem = mockk(relaxed = true)
        val writes = listOf(DynamoWriteTransactItemRequest(transactWriteItem))

        val exception = mockk<TransactionCanceledException>()
        every { exception.cancellationReasons() } returns listOf()
        every { exception.localizedMessage } returns ""
        every { exception.retryable() } returns false

        every { mockDynamoDb.transactWriteItems(any<TransactWriteItemsRequest>())} throws exception

        underTest.executeWriteTransaction(writes)
    }

    @Test(expected = StoreConditionException::class)
    fun canHandleConditionFailedException() {
        val transactWriteItem: TransactWriteItem = mockk(relaxed = true)
        val writes = listOf(DynamoWriteTransactItemRequest(transactWriteItem))

        every { mockDynamoDb.transactWriteItems(any<TransactWriteItemsRequest>())} throws TransactionCanceledException.builder().cancellationReasons(CancellationReason.builder().code("ConditionalCheckFailed").build()).build()

        underTest.executeWriteTransaction(writes)
    }
}
