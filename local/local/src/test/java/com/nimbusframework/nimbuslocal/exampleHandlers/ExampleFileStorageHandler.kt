package com.nimbusframework.nimbuslocal.exampleHandlers

import com.nimbusframework.nimbuscore.annotations.file.FileStorageEventType
import com.nimbusframework.nimbuscore.annotations.function.FileStorageServerlessFunction
import com.nimbusframework.nimbuscore.eventabstractions.FileStorageEvent
import com.nimbusframework.nimbuslocal.exampleModels.Bucket

class ExampleFileStorageHandler {

    @FileStorageServerlessFunction(fileStorageBucket = Bucket::class, eventType = FileStorageEventType.OBJECT_CREATED)
    fun newFile(event: FileStorageEvent) {
        return
    }

    @FileStorageServerlessFunction(fileStorageBucket = Bucket::class, eventType = FileStorageEventType.OBJECT_DELETED)
    fun deletedFile(event: FileStorageEvent) {
        return
    }
}