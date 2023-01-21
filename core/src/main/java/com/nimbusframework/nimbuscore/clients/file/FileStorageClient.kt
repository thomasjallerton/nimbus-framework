package com.nimbusframework.nimbuscore.clients.file

import java.io.File
import java.io.InputStream

interface FileStorageClient {

    fun saveFile(path: String, file: File) = saveFile(path, file, mapOf())
    fun saveFile(path: String, file: File, tags: Map<String, String>)

    fun saveFile(path: String, inputStream: InputStream) = saveFile(path, inputStream, mapOf())
    fun saveFile(path: String, inputStream: InputStream, tags: Map<String, String>)

    fun saveFile(path: String, content: String) = saveFile(path, content, mapOf())
    fun saveFile(path: String, content: String, tags: Map<String, String>)

    fun saveFile(path: String, content: ByteArray) = saveFile(path, content, mapOf())
    fun saveFile(path: String, content: ByteArray, tags: Map<String, String>)

    fun saveFileWithContentType(path: String, content: String, contentType: String) = saveFileWithContentType(path, content, contentType, mapOf())
    fun saveFileWithContentType(path: String, content: String, contentType: String, tags: Map<String, String>)

    fun saveFileWithContentType(path: String, file: File, contentType: String) = saveFileWithContentType(path, file, contentType, mapOf())
    fun saveFileWithContentType(path: String, file: File, contentType: String, tags: Map<String, String>)

    fun saveFileWithContentType(path: String, inputStream: InputStream, contentType: String) = saveFileWithContentType(path, inputStream, contentType, mapOf())
    fun saveFileWithContentType(path: String, inputStream: InputStream, contentType: String, tags: Map<String, String>)

    fun saveFileWithContentType(path: String, content: ByteArray, contentType: String) = saveFileWithContentType(path, content, contentType, mapOf())
    fun saveFileWithContentType(path: String, content: ByteArray, contentType: String, tags: Map<String, String>)

    fun deleteFile(path: String)

    fun listFiles(): List<FileInformation>

    fun getFile(path: String): InputStream

}
