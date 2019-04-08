package localDeployment.exampleHandlers

import com.nimbusframework.nimbuscore.annotation.annotations.file.FileStorageEventType
import com.nimbusframework.nimbuscore.annotation.annotations.function.FileStorageServerlessFunction
import com.nimbusframework.nimbuscore.wrappers.file.models.FileStorageEvent

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