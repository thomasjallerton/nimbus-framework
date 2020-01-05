package com.nimbusframework.nimbuscore.clients.keyvalue

import com.nimbusframework.nimbuscore.examples.KeyValueStoreNoTableName
import com.nimbusframework.nimbuscore.examples.KeyValueStoreWithTableName
import com.nimbusframework.nimbuscore.exceptions.InvalidStageException
import io.kotlintest.specs.AnnotationSpec
import io.kotlintest.shouldBe

class KeyValueStoreDefinitionAnnotationServiceTest : AnnotationSpec() {

    @Test
    fun correctlyGetsNameWhenNoneSet() {
        val tableName = KeyValueStoreAnnotationService.getTableName(KeyValueStoreNoTableName::class.java, "dev")
        tableName shouldBe "KeyValueStoreNoTableNamedev"
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
        KeyValueStoreAnnotationService.getTableName(KeyValueStoreNoTableName::class.java, "prod")
    }

    @Test
    fun correctlyGetsKeyNameAndKeyType() {
        val keyNameAndType = KeyValueStoreAnnotationService.getKeyNameAndType(KeyValueStoreNoTableName::class.java, "dev")
        keyNameAndType.first shouldBe "PrimaryKey"
        keyNameAndType.second shouldBe java.lang.Integer::class.java
    }

    @Test
    fun correctlyGetsKeyNameWhenSet() {
        val keyNameAndType = KeyValueStoreAnnotationService.getKeyNameAndType(KeyValueStoreWithTableName::class.java, "dev")
        keyNameAndType.first shouldBe "test"
    }

}