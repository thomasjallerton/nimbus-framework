package com.nimbusframework.nimbusaws.clients.document

import com.nimbusframework.nimbusaws.examples.document.DocumentStoreNoTableName
import com.nimbusframework.nimbusaws.examples.document.DocumentStoreNoTableNameNoStage
import com.nimbusframework.nimbusaws.examples.document.DocumentStoreWithTableName
import com.nimbusframework.nimbuscore.exceptions.InvalidStageException
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe

class DynamoDbDocumentStoreAnnotationServiceTest : AnnotationSpec() {

    @Test
    fun correctlyGetsNameWhenNoneSet() {
        val tableName = DynamoDbDocumentStoreAnnotationService.getTableName(DocumentStoreNoTableName::class.java, "dev")
        tableName shouldBe "DocumentStoreNoTableNamedev"
    }

    @Test
    fun correctlyGetsNameWhenNoneSetNoStage() {
        val tableName = DynamoDbDocumentStoreAnnotationService.getTableName(DocumentStoreNoTableNameNoStage::class.java, "dev")
        tableName shouldBe "DocumentStoreNoTableNameNoStagedev"
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