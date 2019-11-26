package com.nimbusframework.nimbusaws.clients.dynamo

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.model.PutItemRequest
import com.amazonaws.services.dynamodbv2.model.PutItemResult
import com.google.inject.*
import com.nimbusframework.nimbusaws.AwsClientModule
import com.nimbusframework.nimbusaws.examples.DocumentStoreNoTableName
import io.kotlintest.specs.AnnotationSpec
import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class DynamoClientTest : AnnotationSpec() {

    private lateinit var underTest: DynamoClient
    private lateinit var mockDynamoDb: AmazonDynamoDB

    @BeforeEach
    fun setUp() {
        mockDynamoDb = mockk()
        val injector = Guice.createInjector(object: AbstractModule() {
            override fun configure() {
                bind(AmazonDynamoDB::class.java).toInstance(mockDynamoDb)
            }
        })
        underTest = DynamoClient("test", "test", mapOf())
        injector.injectMembers(underTest)
    }

    @Test
    fun putCorrectlyCallsDynamoDbClient() {
        val obj = DocumentStoreNoTableName("test", 15)

        every { mockDynamoDb.putItem(any()) } returns PutItemResult()

        underTest.put(obj, mapOf())

        verify { mockDynamoDb.putItem(ofType(PutItemRequest::class)) }
    }
}