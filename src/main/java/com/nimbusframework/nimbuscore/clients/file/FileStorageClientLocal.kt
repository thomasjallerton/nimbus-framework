package com.nimbusframework.nimbuscore.clients.file

import com.nimbusframework.nimbuscore.clients.LocalClient
import com.nimbusframework.nimbuscore.clients.PermissionException
import com.nimbusframework.nimbuscore.clients.document.DocumentStoreClient
import com.nimbusframework.nimbuscore.testing.LocalNimbusDeployment
import com.nimbusframework.nimbuscore.testing.function.PermissionType
import java.io.File
import java.io.InputStream

class FileStorageClientLocal(private val bucketName: String): FileStorageClient, LocalClient() {

    private val localNimbusClient = LocalNimbusDeployment.getInstance()
    private val fileStorage = localNimbusClient.getLocalFileStorage(bucketName)

    override fun canUse(): Boolean {
        return checkPermissions(PermissionType.FILE_STORAGE, bucketName)
    }

    override val clientName: String = FileStorageClient::class.java.simpleName

    override fun saveFile(path: String, file: File) {
        checkClientUse()
        fileStorage.saveFile(path, file)
    }

    override fun saveFile(path: String, content: String) {
        checkClientUse()
        fileStorage.saveFile(path, content)
    }

    override fun saveFile(path: String, inputStream: InputStream) {
        checkClientUse()
        fileStorage.saveFile(path, inputStream)
    }

    override fun saveFileWithContentType(path: String, content: String, contentType: String) {
        checkClientUse()
        fileStorage.saveFileWithContentType(path, content, contentType)
    }

    override fun saveFileWithContentType(path: String, file: File, contentType: String) {
        checkClientUse()
        fileStorage.saveFileWithContentType(path, file, contentType)
    }

    override fun saveFileWithContentType(path: String, inputStream: InputStream, contentType: String) {
        checkClientUse()
        fileStorage.saveFileWithContentType(path, inputStream, contentType)
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