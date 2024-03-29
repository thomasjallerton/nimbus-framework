package com.nimbusframework.nimbusaws.wrappers.file

import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification
import com.nimbusframework.nimbuscore.eventabstractions.FileStorageEvent

object S3EventMapper {

    @JvmStatic
    fun getFileStorageEvent(s3Entity: S3EventNotification.S3ObjectEntity, requestId: String): FileStorageEvent {
        return FileStorageEvent(s3Entity.key,
                s3Entity.sizeAsLong,
                requestId)
    }

}
