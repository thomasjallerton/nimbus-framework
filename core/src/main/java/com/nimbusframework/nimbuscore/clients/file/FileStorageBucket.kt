package com.nimbusframework.nimbuscore.clients.file

import com.nimbusframework.nimbuscore.clients.ClientBuilder
import java.io.File
import java.io.InputStream

open class FileStorageBucket(fileStorageBucket: Class<*>): FileStorageClient {

    private val fileStorageClient = ClientBuilder.getFileStorageClient(fileStorageBucket)

    override fun saveFile(path: String, file: File) {
        fileStorageClient.saveFile(path, file)
    }

    override fun saveFile(path: String, inputStream: InputStream) {
        fileStorageClient.saveFile(path, inputStream)
    }

    override fun saveFile(path: String, content: String) {
        fileStorageClient.saveFile(path, content)
    }

    override fun saveFileWithContentType(path: String, content: String, contentType: String) {
        fileStorageClient.saveFileWithContentType(path, content, contentType)
    }

    override fun saveFileWithContentType(path: String, file: File, contentType: String) {
        fileStorageClient.saveFileWithContentType(path, file, contentType)
    }

    override fun saveFileWithContentType(path: String, inputStream: InputStream, contentType: String) {
        fileStorageClient.saveFileWithContentType(path, inputStream, contentType)
    }

    override fun deleteFile(path: String) {
        fileStorageClient.deleteFile(path)
    }

    override fun listFiles(): List<FileInformation> {
        return fileStorageClient.listFiles()
    }

    override fun getFile(path: String): InputStream {
        return fileStorageClient.getFile(path)
    }

}
