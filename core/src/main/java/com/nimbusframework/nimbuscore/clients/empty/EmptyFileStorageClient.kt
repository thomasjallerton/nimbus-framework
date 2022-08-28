package com.nimbusframework.nimbuscore.clients.empty

import com.nimbusframework.nimbuscore.clients.file.FileInformation
import com.nimbusframework.nimbuscore.clients.file.FileStorageClient
import com.nimbusframework.nimbuscore.exceptions.PermissionException
import java.io.File
import java.io.InputStream

class EmptyFileStorageClient(): FileStorageClient {

    private val clientName = "FileStorageClient"

    override fun saveFile(path: String, file: File, tags: Map<String, String>) {
        throw PermissionException(clientName)
    }

    override fun saveFile(path: String, inputStream: InputStream, tags: Map<String, String>) {
        throw PermissionException(clientName)
    }

    override fun saveFile(path: String, content: String, tags: Map<String, String>) {
        throw PermissionException(clientName)
    }

    override fun saveFileWithContentType(path: String, content: String, contentType: String, tags: Map<String, String>) {
        throw PermissionException(clientName)
    }

    override fun saveFileWithContentType(path: String, file: File, contentType: String, tags: Map<String, String>) {
        throw PermissionException(clientName)
    }

    override fun saveFileWithContentType(path: String, inputStream: InputStream, contentType: String, tags: Map<String, String>) {
        throw PermissionException(clientName)
    }

    override fun deleteFile(path: String) {
        throw PermissionException(clientName)
    }

    override fun listFiles(): List<FileInformation> {
        throw PermissionException(clientName)
    }

    override fun getFile(path: String): InputStream {
        throw PermissionException(clientName)
    }
}
