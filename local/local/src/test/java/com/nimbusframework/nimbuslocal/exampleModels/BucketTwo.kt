package com.nimbusframework.nimbuslocal.exampleModels

import com.nimbusframework.nimbuscore.annotations.deployment.AfterDeployment
import com.nimbusframework.nimbuscore.annotations.file.FileStorageBucketDefinition
import com.nimbusframework.nimbuscore.annotations.file.UsesFileStorageBucket
import com.nimbusframework.nimbuscore.clients.ClientBuilder

@FileStorageBucketDefinition(bucketName = "BucketTwo", staticWebsite = true)
class BucketTwo {

    @AfterDeployment
    @UsesFileStorageBucket(fileStorageBucket = BucketTwo::class)
    fun uploadFile() {
        val client = ClientBuilder.getFileStorageClient(BucketTwo::class.java)
        client.saveFile("test.txt", "HELLO WORLD")
    }
}