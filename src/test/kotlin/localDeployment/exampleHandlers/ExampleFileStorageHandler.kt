package localDeployment.exampleHandlers

import annotation.annotations.file.FileStorageEventType
import annotation.annotations.function.FileStorageServerlessFunction
import wrappers.file.models.FileStorageEvent

class ExampleFileStorageHandler {

    @FileStorageServerlessFunction(bucketName = "testbucket", eventType = FileStorageEventType.OBJECT_CREATED)
    fun newFile(event: FileStorageEvent) {
        return
    }

    @FileStorageServerlessFunction(bucketName = "testbucket", eventType = FileStorageEventType.OBJECT_DELETED)
    fun deletedFile(event: FileStorageEvent) {
        return
    }
}