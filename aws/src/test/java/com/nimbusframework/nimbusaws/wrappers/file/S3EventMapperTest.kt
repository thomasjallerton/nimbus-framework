package com.nimbusframework.nimbusaws.wrappers.file

import com.amazonaws.services.s3.event.S3EventNotification
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe

class S3EventMapperTest: AnnotationSpec() {

    val underTest = S3EventMapper

    @Test
    fun canParseS3Event() {
        val s3Object = S3EventNotification.S3ObjectEntity("testKey", 1000, "", "", "")
        val result = underTest.getFileStorageEvent(s3Object, "requestId")
        result.key shouldBe "testKey"
        result.size shouldBe 1000
        result.requestId shouldBe "requestId"
    }
}