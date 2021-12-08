package com.nimbusframework.nimbuslocal.deployment.services.resource

import com.nimbusframework.nimbuscore.annotations.deployment.FileUpload
import com.nimbusframework.nimbuscore.annotations.file.FileStorageBucketDefinition
import com.nimbusframework.nimbuscore.clients.file.FileStorageBucketNameAnnotationService
import com.nimbusframework.nimbuscore.persisted.FileUploadDescription
import com.nimbusframework.nimbuslocal.deployment.file.LocalFileStorage
import com.nimbusframework.nimbuslocal.deployment.services.LocalResourceHolder
import com.nimbusframework.nimbuslocal.deployment.services.StageService
import com.nimbusframework.nimbuslocal.deployment.webserver.WebServerHandler

class LocalFileStorageCreator(
        private val localResourceHolder: LocalResourceHolder,
        private val httpPort: Int,
        private val variableSubstitution: MutableMap<String, String>,
        private val fileUploadDetails: MutableMap<Class<*>, MutableList<FileUploadDescription>>,
        private val stageService: StageService
) : LocalCreateResourcesHandler {

    override fun createResource(clazz: Class<out Any>) {
        val fileStorageBuckets = clazz.getAnnotationsByType(FileStorageBucketDefinition::class.java)
        val localWebservers = localResourceHolder.httpServers

        val fileStorageBucket = stageService.annotationForStage(fileStorageBuckets) { annotation -> annotation.stages}
        if (fileStorageBucket != null) {
            if (fileStorageBucket.staticWebsite && !localWebservers.containsKey(fileStorageBucket.bucketName)) {
                val localWebserver = WebServerHandler(fileStorageBucket.indexFile, fileStorageBucket.errorFile, "http://localhost:$httpPort/${fileStorageBucket.bucketName}/")
                localWebservers[fileStorageBucket.bucketName] = localWebserver
                variableSubstitution["\${${fileStorageBucket.bucketName.toUpperCase()}_URL}"] = "http://localhost:$httpPort/${fileStorageBucket.bucketName}"
            }

            val fileStorage = localResourceHolder.fileStorage
            if (!fileStorage.containsKey(fileStorageBucket.bucketName)) {
                val allowedOrigins = fileStorageBucket.allowedCorsOrigins.map {
                    if (it == "#{NIMBUS_REST_API_URL}") {
                        "http://localhost:$httpPort/function/"
                    } else {
                        it
                    }
                }

                fileStorage[fileStorageBucket.bucketName] = LocalFileStorage(fileStorageBucket.bucketName, allowedOrigins)
            }
        }

        val fileUpload = stageService.annotationForStage(clazz.getAnnotationsByType(FileUpload::class.java)) { annotation -> annotation.stages}
        if (fileUpload != null) {
            val bucketFiles = fileUploadDetails.getOrPut(fileUpload.fileStorageBucket.java) { mutableListOf() }
            val description = FileUploadDescription(fileUpload.localPath, fileUpload.targetPath, fileUpload.substituteNimbusVariables)
            bucketFiles.add(description)
        }
    }

}