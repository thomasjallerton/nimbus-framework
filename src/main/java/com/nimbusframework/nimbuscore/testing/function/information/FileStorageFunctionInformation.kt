package com.nimbusframework.nimbuscore.testing.function.information

import com.nimbusframework.nimbuscore.annotation.annotations.file.FileStorageEventType
import com.nimbusframework.nimbuscore.testing.function.FunctionType

data class FileStorageFunctionInformation(
        val bucketName: String,
        val eventType: FileStorageEventType
): FunctionInformation(FunctionType.FILE_STORAGE)