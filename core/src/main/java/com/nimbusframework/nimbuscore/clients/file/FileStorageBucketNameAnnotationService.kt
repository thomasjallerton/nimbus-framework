package com.nimbusframework.nimbuscore.clients.file

import com.nimbusframework.nimbuscore.annotations.file.FileStorageBucketDefinition
import com.nimbusframework.nimbuscore.exceptions.InvalidStageException

object FileStorageBucketNameAnnotationService {

    fun getBucketName(clazz: Class<*>, stage: String): String {
        val fileStorageBuckets = clazz.getDeclaredAnnotationsByType(FileStorageBucketDefinition::class.java)
        for (fileStorageBucket in fileStorageBuckets) {
            for (annotationStage in fileStorageBucket.stages) {
                if (annotationStage == stage) {
                    return fileStorageBucket.bucketName
                }
            }
        }
        throw InvalidStageException()
    }

}