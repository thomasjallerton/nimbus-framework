package com.nimbusframework.nimbusaws.clients.document

import com.nimbusframework.nimbusaws.examples.DocumentStoreNoTableName
import com.nimbusframework.nimbusaws.examples.DocumentStoreWithTableName
import com.nimbusframework.nimbuscore.exceptions.InvalidStageException
import io.kotlintest.specs.AnnotationSpec
import io.kotlintest.shouldBe

class DynamoDbDocumentStoreAnnotationServiceTest : AnnotationSpec() {

    @Test
    fun correctlyGetsNameWhenNoneSet() {
        val tableName = DynamoDbDocumentStoreAnnotationService.getTableName(DocumentStoreNoTableName::class.java, "dev")
        tableName shouldBe "DocumentStoreNoTableNamedev"
    }

    @Test
    fun correctlyGetsNameWhenSet() {
        val tableName = DynamoDbDocumentStoreAnnotationService.getTableName(DocumentStoreWithTableName::class.java, "dev")
        tableName shouldBe "testdev"
    }

    @Test(expected = InvalidStageException::class)
    fun throwsExceptionWhenWrongStage() {
        DynamoDbDocumentStoreAnnotationService.getTableName(DocumentStoreNoTableName::class.java, "prod")
    }

}