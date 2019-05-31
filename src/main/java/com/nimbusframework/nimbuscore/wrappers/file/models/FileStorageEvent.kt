package com.nimbusframework.nimbuscore.wrappers.file.models

import com.amazonaws.services.s3.event.S3EventNotification
import com.nimbusframework.nimbuscore.wrappers.ServerlessEvent
import java.util.*

data class FileStorageEvent(
    val key: String?,
    val size: Long?,
    val requestId: String = UUID.randomUUID().toString()
): ServerlessEvent {

    constructor(s3Entity: S3EventNotification.S3ObjectEntity, requestId: String): this(
            s3Entity.key,
            s3Entity.sizeAsLong,
            requestId
    )

}