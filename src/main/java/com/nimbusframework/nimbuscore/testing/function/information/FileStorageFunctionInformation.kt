package com.nimbusframework.nimbuscore.testing.function.information

import com.nimbusframework.nimbuscore.annotation.annotations.file.FileStorageEventType

data class FileStorageFunctionInformation(
        val bucketName: String,
        val eventType: FileStorageEventType
): FunctionInformation()