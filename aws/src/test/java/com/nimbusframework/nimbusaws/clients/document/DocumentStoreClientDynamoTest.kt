package com.nimbusframework.nimbusaws.clients.document

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.nimbusframework.nimbusaws.AwsClientModule
import com.nimbusframework.nimbusaws.clients.dynamo.DynamoClient
import com.nimbusframework.nimbusaws.examples.DocumentStoreNoTableName
import io.kotlintest.specs.AnnotationSpec
import io.kotlintest.shouldBe
import io.mockk.mockk

class DocumentStoreClientDynamoTest : AnnotationSpec() {

    private lateinit var underTest: DocumentStoreClientDynamo<DocumentStoreNoTableName>
    private lateinit var mockDynamoDb: AmazonDynamoDB

    @BeforeEach
    fun setUp() {
        mockDynamoDb = mockk()
        val injector = Guice.createInjector(AwsClientModule())
        underTest = DocumentStoreClientDynamo(DocumentStoreNoTableName::class.java, "test", "dev")
        injector.injectMembers(underTest)
    }

    fun test() {
        underTest.put(DocumentStoreNoTableName("test", 17))
    }

}