package com.nimbusframework.nimbuscore.testing.services.resource

import com.nimbusframework.nimbuscore.annotation.annotations.deployment.FileUpload
import com.nimbusframework.nimbuscore.annotation.annotations.file.FileStorageBucket
import com.nimbusframework.nimbuscore.annotation.annotations.file.UsesFileStorageClient
import com.nimbusframework.nimbuscore.persisted.FileUploadDescription
import com.nimbusframework.nimbuscore.testing.file.LocalFileStorage
import com.nimbusframework.nimbuscore.testing.services.LocalResourceHolder
import com.nimbusframework.nimbuscore.testing.webserver.WebserverHandler

class LocalFileStorageCreator(
        private val localResourceHolder: LocalResourceHolder,
        private val httpPort: Int,
        private val variableSubstitution: MutableMap<String, String>,
        private val fileUploadDetails: MutableMap<String, MutableList<FileUploadDescription>>,
        private val stage: String
) : LocalCreateResourcesHandler {

    override fun createResource(clazz: Class<out Any>) {
        val fileStorageBuckets = clazz.getAnnotationsByType(FileStorageBucket::class.java)
        val localWebservers = localResourceHolder.httpServers

        for (fileStorageBucket in fileStorageBuckets) {
            if (fileStorageBucket.stages.contains(stage)) {
                if (fileStorageBucket.staticWebsite && !localWebservers.containsKey(fileStorageBucket.bucketName)) {
                    val localWebserver = WebserverHandler(fileStorageBucket.indexFile, fileStorageBucket.errorFile)
                    localWebservers[fileStorageBucket.bucketName] = localWebserver
                    variableSubstitution["\${${fileStorageBucket.bucketName.toUpperCase()}_URL}"] = "http://localhost:$httpPort/${fileStorageBucket.bucketName}"
                }

                val fileStorage = localResourceHolder.fileStorage
                if (!fileStorage.containsKey(fileStorageBucket.bucketName)) {
                    fileStorage[fileStorageBucket.bucketName] = LocalFileStorage(fileStorageBucket.bucketName)
                }
            }
        }

        for (method in clazz.methods) {
            val usesFileStorages = method.getAnnotationsByType(UsesFileStorageClient::class.java)

            val fileStorage = localResourceHolder.fileStorage

            for (usesFileStorage in usesFileStorages) {
                if (usesFileStorage.stages.contains(stage)) {
                    if (!fileStorage.containsKey(usesFileStorage.bucketName)) {
                        fileStorage[usesFileStorage.bucketName] = LocalFileStorage(usesFileStorage.bucketName)
                    }
                }
            }
        }

        for (fileUpload in clazz.getAnnotationsByType(FileUpload::class.java)) {
            if (fileUpload.stages.contains(stage)) {
                val bucketFiles = fileUploadDetails.getOrPut(fileUpload.bucketName) { mutableListOf() }
                val description = FileUploadDescription(fileUpload.localPath, fileUpload.targetPath, fileUpload.substituteNimbusVariables)
                bucketFiles.add(description)
            }
        }
    }

}