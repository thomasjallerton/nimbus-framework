package com.nimbusframework.nimbuscore.clients.file

import com.nimbusframework.nimbuscore.examples.filestorage.FileStorage
import com.nimbusframework.nimbuscore.examples.filestorage.FileStorageNoStage
import com.nimbusframework.nimbuscore.exceptions.InvalidStageException
import io.kotlintest.shouldBe
import io.kotlintest.specs.AnnotationSpec

internal class FileStorageBucketNameAnnotationServiceTest : AnnotationSpec() {

    @Test
    fun correctlyGetsBucketName() {
        val bucketName = FileStorageBucketNameAnnotationService.getBucketName(FileStorage::class.java, "dev")
        bucketName shouldBe "bucket"
    }

    @Test
    fun correctlyGetsBucketNameWhenNoStageSet() {
        val bucketName = FileStorageBucketNameAnnotationService.getBucketName(FileStorageNoStage::class.java, "dev")
        bucketName shouldBe "bucket"
    }

    @Test(expected = InvalidStageException::class)
    fun throwsExceptionWhenWrongStage() {
        FileStorageBucketNameAnnotationService.getBucketName(FileStorage::class.java, "prod")
    }

}