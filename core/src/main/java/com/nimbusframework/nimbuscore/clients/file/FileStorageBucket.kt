package com.nimbusframework.nimbuscore.clients.file

import com.nimbusframework.nimbuscore.clients.ClientBuilder
import java.io.File
import java.io.InputStream

open class FileStorageBucket(fileStorageBucket: Class<*>): FileStorageClient {

    private val fileStorageClient = ClientBuilder.getFileStorageClient(fileStorageBucket)

    override fun saveFile(path: String, file: File, tags: Map<String, String>) {
        fileStorageClient.saveFile(path, file, tags)
    }

    override fun saveFile(path: String, inputStream: InputStream, tags: Map<String, String>) {
        fileStorageClient.saveFile(path, inputStream, tags)
    }

    override fun saveFile(path: String, content: String, tags: Map<String, String>) {
        fileStorageClient.saveFile(path, content, tags)
    }

    override fun saveFileWithContentType(path: String, content: String, contentType: String, tags: Map<String, String>) {
        fileStorageClient.saveFileWithContentType(path, content, contentType, tags)
    }

    override fun saveFileWithContentType(path: String, file: File, contentType: String, tags: Map<String, String>) {
        fileStorageClient.saveFileWithContentType(path, file, contentType, tags)
    }

    override fun saveFileWithContentType(path: String, inputStream: InputStream, contentType: String, tags: Map<String, String>) {
        fileStorageClient.saveFileWithContentType(path, inputStream, contentType, tags)
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
