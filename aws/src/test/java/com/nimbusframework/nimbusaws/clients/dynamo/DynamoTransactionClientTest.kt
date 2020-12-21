package com.nimbusframework.nimbusaws.clients.dynamo

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.model.*
import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.nimbusframework.nimbuscore.exceptions.NonRetryableException
import com.nimbusframework.nimbuscore.exceptions.RetryableException
import com.nimbusframework.nimbuscore.exceptions.StoreConditionException
import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.specs.AnnotationSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot

class DynamoTransactionClientTest : AnnotationSpec() {

    private lateinit var underTest: DynamoTransactionClient
    private lateinit var mockDynamoDb: AmazonDynamoDB

    @BeforeEach
    fun setup() {
        mockDynamoDb = mockk(relaxed = true)
        val injector = Guice.createInjector(object: AbstractModule() {
            override fun configure() {
                bind(AmazonDynamoDB::class.java).toInstance(mockDynamoDb)
            }
        })
        underTest = DynamoTransactionClient()
        injector.injectMembers(underTest)
    }

    @Test
    fun canExecuteDynamoWriteTransactions() {
        val transactWriteItem: TransactWriteItem = mockk(relaxed = true)
        val writes = listOf(DynamoWriteTransactItemRequest(transactWriteItem))

        val request = slot<TransactWriteItemsRequest>()

        every { mockDynamoDb.transactWriteItems(capture(request))} returns null

        underTest.executeWriteTransaction(writes)

        request.captured.transactItems shouldContain transactWriteItem
    }

    @Test
    fun canExecuteDynamoGetTransactions() {
        val transactGetItem: TransactGetItem = mockk(relaxed = true)
        val reads = listOf(DynamoReadItemRequest(transactGetItem) {"Test"})

        val request = slot<TransactGetItemsRequest>()

        every { mockDynamoDb.transactGetItems(capture(request))} returns TransactGetItemsResult().withResponses(ItemResponse().withItem(mapOf()))

        underTest.executeReadTransaction(reads) shouldContain "Test"

        request.captured.transactItems shouldContain transactGetItem
    }

    @Test(expected = RetryableException::class)
    fun canHandleRetryableException() {
        val transactWriteItem: TransactWriteItem = mockk(relaxed = true)
        val writes = listOf(DynamoWriteTransactItemRequest(transactWriteItem))

        val exception = mockk<TransactionCanceledException>()
        every { exception.cancellationReasons } returns listOf()
        every { exception.localizedMessage } returns ""
        every { exception.isRetryable } returns true

        every { mockDynamoDb.transactWriteItems(any())} throws exception

        underTest.executeWriteTransaction(writes)
    }

    @Test(expected = NonRetryableException::class)
    fun canHandleNonRetryableException() {
        val transactWriteItem: TransactWriteItem = mockk(relaxed = true)
        val writes = listOf(DynamoWriteTransactItemRequest(transactWriteItem))

        val exception = mockk<TransactionCanceledException>()
        every { exception.cancellationReasons } returns listOf()
        every { exception.localizedMessage } returns ""
        every { exception.isRetryable } returns false

        every { mockDynamoDb.transactWriteItems(any())} throws exception

        underTest.executeWriteTransaction(writes)
    }

    @Test(expected = StoreConditionException::class)
    fun canHandleConditionFailedException() {
        val transactWriteItem: TransactWriteItem = mockk(relaxed = true)
        val writes = listOf(DynamoWriteTransactItemRequest(transactWriteItem))

        every { mockDynamoDb.transactWriteItems(any())} throws TransactionCanceledException("").withCancellationReasons(CancellationReason().withCode("ConditionalCheckFailed"))

        underTest.executeWriteTransaction(writes)
    }
}