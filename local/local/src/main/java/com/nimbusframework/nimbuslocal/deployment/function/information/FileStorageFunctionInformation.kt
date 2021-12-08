package com.nimbusframework.nimbuslocal.deployment.function.information

import com.nimbusframework.nimbuscore.annotations.file.FileStorageEventType
import com.nimbusframework.nimbuslocal.deployment.function.FunctionType

data class FileStorageFunctionInformation(
        val bucketName: String,
        val eventType: FileStorageEventType
): FunctionInformation(FunctionType.FILE_STORAGE)