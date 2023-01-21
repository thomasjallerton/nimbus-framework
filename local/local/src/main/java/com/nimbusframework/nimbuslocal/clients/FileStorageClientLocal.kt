package com.nimbusframework.nimbuslocal.clients

import com.nimbusframework.nimbuscore.clients.file.FileInformation
import com.nimbusframework.nimbuscore.clients.file.FileStorageBucketNameAnnotationService
import com.nimbusframework.nimbuscore.clients.file.FileStorageClient
import com.nimbusframework.nimbuscore.permissions.PermissionType
import com.nimbusframework.nimbuslocal.LocalNimbusDeployment
import java.io.File
import java.io.InputStream

class FileStorageClientLocal(bucketClass: Class<*>, stage: String): FileStorageClient, LocalClient(PermissionType.FILE_STORAGE) {

    private val bucketName = FileStorageBucketNameAnnotationService.getBucketName(bucketClass, stage)
    private val localNimbusClient = LocalNimbusDeployment.getInstance()
    private val fileStorage = localNimbusClient.getLocalFileStorage(bucketClass)

    override fun canUse(permissionType: PermissionType): Boolean {
        return checkPermissions(permissionType, bucketName)
    }

    override val clientName: String = FileStorageClient::class.java.simpleName

    override fun saveFile(path: String, file: File, tags: Map<String, String>) {
        checkClientUse()
        fileStorage.saveFile(path, file, tags)
    }

    override fun saveFile(path: String, content: String, tags: Map<String, String>) {
        checkClientUse()
        fileStorage.saveFile(path, content, tags)
    }

    override fun saveFile(path: String, content: ByteArray, tags: Map<String, String>) {
        checkClientUse()
        fileStorage.saveFile(path, content, tags)
    }

    override fun saveFile(path: String, inputStream: InputStream, tags: Map<String, String>) {
        checkClientUse()
        fileStorage.saveFile(path, inputStream, tags)
    }

    override fun saveFileWithContentType(path: String, content: String, contentType: String, tags: Map<String, String>) {
        checkClientUse()
        fileStorage.saveFileWithContentType(path, content, contentType, tags)
    }

    override fun saveFileWithContentType(path: String, file: File, contentType: String, tags: Map<String, String>) {
        checkClientUse()
        fileStorage.saveFileWithContentType(path, file, contentType, tags)
    }

    override fun saveFileWithContentType(path: String, inputStream: InputStream, contentType: String, tags: Map<String, String>) {
        checkClientUse()
        fileStorage.saveFileWithContentType(path, inputStream, contentType, tags)
    }

    override fun saveFileWithContentType(path: String, content: ByteArray, contentType: String, tags: Map<String, String>) {
        checkClientUse()
        fileStorage.saveFileWithContentType(path, content, contentType, tags)
    }

    override fun deleteFile(path: String) {
        checkClientUse()
        fileStorage.deleteFile(path)
    }

    override fun listFiles(): List<FileInformation> {
        checkClientUse()
        return fileStorage.listFiles()
    }

    override fun getFile(path: String): InputStream {
        checkClientUse()
        return fileStorage.getFile(path)
    }
}
