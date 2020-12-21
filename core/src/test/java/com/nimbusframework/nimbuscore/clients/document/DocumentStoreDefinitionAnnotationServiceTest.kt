package com.nimbusframework.nimbuscore.clients.document

import com.nimbusframework.nimbuscore.examples.document.DocumentStoreNoTableName
import com.nimbusframework.nimbuscore.examples.document.DocumentStoreNoTableNameNoStage
import com.nimbusframework.nimbuscore.examples.document.DocumentStoreWithTableName
import com.nimbusframework.nimbuscore.exceptions.InvalidStageException
import io.kotlintest.specs.AnnotationSpec
import io.kotlintest.shouldBe

class DocumentStoreDefinitionAnnotationServiceTest : AnnotationSpec() {

    @Test
    fun correctlyGetsNameWhenNoneSet() {
        val tableName = DocumentStoreAnnotationService.getTableName(DocumentStoreNoTableName::class.java, "dev")
        tableName shouldBe "DocumentStoreNoTableNamedev"
    }

    @Test
    fun correctlyGetsNameWhenNoneSetNoStage() {
        val tableName = DocumentStoreAnnotationService.getTableName(DocumentStoreNoTableNameNoStage::class.java, "dev")
        tableName shouldBe "DocumentStoreNoTableNameNoStagedev"
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