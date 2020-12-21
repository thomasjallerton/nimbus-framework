package com.nimbusframework.nimbusaws.clients.keyvalue

import com.nimbusframework.nimbusaws.examples.keyvalue.KeyValueStoreNoTableName
import com.nimbusframework.nimbusaws.examples.keyvalue.KeyValueStoreNoTableNameNoStage
import com.nimbusframework.nimbusaws.examples.keyvalue.KeyValueStoreWithTableName
import com.nimbusframework.nimbuscore.exceptions.InvalidStageException
import io.kotlintest.shouldBe
import io.kotlintest.specs.AnnotationSpec

class DynamoDbKeyValueStoreAnnotationServiceTest : AnnotationSpec() {

    @Test
    fun correctlyGetsNameWhenNoneSet() {
        val tableName = DynamoDbKeyValueStoreAnnotationService.getTableName(KeyValueStoreNoTableName::class.java, "dev")
        tableName shouldBe "KeyValueStoreNoTableNamedev"
    }

    @Test
    fun correctlyGetsNameWhenNoneSetNoStage() {
        val tableName = DynamoDbKeyValueStoreAnnotationService.getTableName(KeyValueStoreNoTableNameNoStage::class.java, "dev")
        tableName shouldBe "KeyValueStoreNoTableNameNoStagedev"
    }

    @Test
    fun correctlyGetsNameWhenSet() {
        val tableName = DynamoDbKeyValueStoreAnnotationService.getTableName(KeyValueStoreWithTableName::class.java, "dev")
        tableName shouldBe "testdev"
    }

    @Test(expected = InvalidStageException::class)
    fun throwsExceptionWhenWrongStage() {
        DynamoDbKeyValueStoreAnnotationService.getTableName(KeyValueStoreNoTableName::class.java, "prod")
    }

    @Test(expected = InvalidStageException::class)
    fun throwsExceptionWhenWrongStageWhenFetchingNameAndType() {
        DynamoDbKeyValueStoreAnnotationService.getKeyNameAndType(KeyValueStoreNoTableName::class.java, "prod")
    }

    @Test
    fun correctlyGetsKeyNameAndKeyType() {
        val keyNameAndType = DynamoDbKeyValueStoreAnnotationService.getKeyNameAndType(KeyValueStoreNoTableName::class.java, "dev")
        keyNameAndType.first shouldBe "PrimaryKey"
        keyNameAndType.second shouldBe java.lang.Integer::class.java
    }

    @Test
    fun correctlyGetsKeyNameAndKeyTypeNoStage() {
        val keyNameAndType = DynamoDbKeyValueStoreAnnotationService.getKeyNameAndType(KeyValueStoreNoTableNameNoStage::class.java, "dev")
        keyNameAndType.first shouldBe "PrimaryKey"
        keyNameAndType.second shouldBe java.lang.Integer::class.java
    }

    @Test
    fun correctlyGetsKeyNameWhenSet() {
        val keyNameAndType = DynamoDbKeyValueStoreAnnotationService.getKeyNameAndType(KeyValueStoreWithTableName::class.java, "dev")
        keyNameAndType.first shouldBe "test"
    }

}