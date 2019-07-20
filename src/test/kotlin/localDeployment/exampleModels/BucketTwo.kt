package localDeployment.exampleModels

import com.nimbusframework.nimbuscore.annotation.annotations.deployment.AfterDeployment
import com.nimbusframework.nimbuscore.annotation.annotations.file.FileStorageBucket
import com.nimbusframework.nimbuscore.annotation.annotations.file.UsesFileStorage
import com.nimbusframework.nimbuscore.clients.ClientBuilder

@FileStorageBucket(bucketName = "BucketTwo", staticWebsite = true)
class BucketTwo {
    @AfterDeployment
    @UsesFileStorage(bucketName = "BucketTwo")
    fun uploadFile() {
        val client = ClientBuilder.getFileStorageClient("BucketTwo")
        client.saveFile("test.txt", "HELLO WORLD")
    }
}