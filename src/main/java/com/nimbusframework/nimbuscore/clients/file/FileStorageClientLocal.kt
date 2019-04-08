package com.nimbusframework.nimbuscore.clients.file

import com.nimbusframework.nimbuscore.testing.LocalNimbusDeployment
import java.io.File
import java.io.InputStream

class FileStorageClientLocal(bucketName: String): FileStorageClient {

    private val localNimbusClient = LocalNimbusDeployment.getInstance()
    private val fileStorage = localNimbusClient.getLocalFileStorage(bucketName)

    override fun saveFile(path: String, file: File) {
        fileStorage.saveFile(path, file)
    }

    override fun saveFile(path: String, content: String) {
        fileStorage.saveFile(path, content)
    }

    override fun saveFile(path: String, inputStream: InputStream) {
        fileStorage.saveFile(path, inputStream)
    }

    override fun saveFileWithContentType(path: String, content: String, contentType: String) {
        fileStorage.saveFileWithContentType(path, content, contentType)
    }

    override fun saveFileWithContentType(path: String, file: File, contentType: String) {
        fileStorage.saveFileWithContentType(path, file, contentType)
    }

    override fun saveFileWithContentType(path: String, inputStream: InputStream, contentType: String) {
        fileStorage.saveFileWithContentType(path, inputStream, contentType)
    }

    override fun deleteFile(path: String) {
        fileStorage.deleteFile(path)
    }

    override fun listFiles(): List<FileInformation> {
        return fileStorage.listFiles()
    }

    override fun getFile(path: String): InputStream {
        return fileStorage.getFile(path)
    }
}