package com.nimbusframework.nimbuscore.clients.file

import com.nimbusframework.nimbuscore.annotations.file.FileStorageBucketDefinition
import com.nimbusframework.nimbuscore.exceptions.InvalidStageException

object FileStorageBucketNameAnnotationService {

    fun getBucketName(clazz: Class<*>, stage: String): String {
        val fileStorageBuckets = clazz.getDeclaredAnnotationsByType(FileStorageBucketDefinition::class.java)
        // Attempt to find specific annotation for this stage. If none exist then there is one annotation that has no stage (so uses the defaults)
        for (fileStorageBucket in fileStorageBuckets) {
            if (fileStorageBucket.stages.contains(stage)) {
                return fileStorageBucket.bucketName
            }
        }
        val fileStorageBucket = fileStorageBuckets.firstOrNull { it.stages.isEmpty() } ?: throw InvalidStageException()
        return fileStorageBucket.bucketName
    }

}