package com.nimbusframework.nimbuscore.clients.document

import com.nimbusframework.nimbuscore.examples.DocumentStoreNoTableName
import com.nimbusframework.nimbuscore.examples.DocumentStoreWithTableName
import com.nimbusframework.nimbuscore.exceptions.InvalidStageException
import io.kotlintest.specs.AnnotationSpec
import io.kotlintest.shouldBe

class DocumentStoreAnnotationServiceTest : AnnotationSpec() {

    @Test
    fun correctlyGetsNameWhenNoneSet() {
        val tableName = DocumentStoreAnnotationService.getTableName(DocumentStoreNoTableName::class.java, "dev")
        tableName shouldBe "DocumentStoreNoTableNamedev"
    }

    @Test
    fun correctlyGetsNameWhenSet() {
        val tableName = DocumentStoreAnnotationService.getTableName(DocumentStoreWithTableName::class.java, "dev")
        tableName shouldBe "testdev"
    }

    @Test(expected = InvalidStageException::class)
    fun throwsExceptionWhenWrongStage() {
        DocumentStoreAnnotationService.getTableName(DocumentStoreNoTableName::class.java, "prod")
    }

}