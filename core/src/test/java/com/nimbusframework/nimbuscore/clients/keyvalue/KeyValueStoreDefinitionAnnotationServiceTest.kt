package com.nimbusframework.nimbuscore.clients.keyvalue

import com.nimbusframework.nimbuscore.examples.keyvalue.KeyValueStoreNoTableName
import com.nimbusframework.nimbuscore.examples.keyvalue.KeyValueStoreNoTableNameNoStage
import com.nimbusframework.nimbuscore.examples.keyvalue.KeyValueStoreWithTableName
import com.nimbusframework.nimbuscore.exceptions.InvalidStageException
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe

class KeyValueStoreDefinitionAnnotationServiceTest : AnnotationSpec() {

    @Test
    fun correctlyGetsNameWhenNoneSet() {
        val tableName = KeyValueStoreAnnotationService.getTableName(KeyValueStoreNoTableName::class.java, "dev")
        tableName shouldBe "KeyValueStoreNoTableNamedev"
    }

    @Test
    fun correctlyGetsNameWhenNoStageSet() {
        val tableName = KeyValueStoreAnnotationService.getTableName(KeyValueStoreNoTableNameNoStage::class.java, "dev")
        tableName shouldBe "KeyValueStoreNoTableNameNoStagedev"
    }

    @Test
    fun correctlyGetsNameWhenSet() {
        val tableName = KeyValueStoreAnnotationService.getTableName(KeyValueStoreWithTableName::class.java, "dev")
        tableName shouldBe "testdev"
    }

    @Test(expected = InvalidStageException::class)
    fun throwsExceptionWhenWrongStage() {
        KeyValueStoreAnnotationService.getTableName(KeyValueStoreNoTableName::class.java, "prod")
    }

    @Test(expected = InvalidStageException::class)
    fun throwsExceptionWhenWrongStageWhenFetchingNameAndType() {
        KeyValueStoreAnnotationService.getKeyNameAndType(KeyValueStoreNoTableName::class.java, "prod")
    }

    @Test
    fun correctlyGetsKeyNameAndKeyType() {
        val keyNameAndType = KeyValueStoreAnnotationService.getKeyNameAndType(KeyValueStoreNoTableName::class.java, "dev")
        keyNameAndType.first shouldBe "PrimaryKey"
        keyNameAndType.second shouldBe java.lang.Integer::class.java
    }

    @Test
    fun correctlyGetsKeyNameAndKeyTypeWhenNoStageSet() {
        val keyNameAndType = KeyValueStoreAnnotationService.getKeyNameAndType(KeyValueStoreNoTableNameNoStage::class.java, "dev")
        keyNameAndType.first shouldBe "PrimaryKey"
        keyNameAndType.second shouldBe java.lang.Integer::class.java
    }

    @Test
    fun correctlyGetsKeyNameWhenSet() {
        val keyNameAndType = KeyValueStoreAnnotationService.getKeyNameAndType(KeyValueStoreWithTableName::class.java, "dev")
        keyNameAndType.first shouldBe "test"
    }

}