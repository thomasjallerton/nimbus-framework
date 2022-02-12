package com.nimbusframework.nimbuslocal.deployment.services

import com.nimbusframework.nimbuscore.clients.ClientBuilder
import com.nimbusframework.nimbuscore.clients.file.FileStorageClient
import com.nimbusframework.nimbuscore.persisted.FileUploadDescription
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern

class FileService(
        private val variableSubstitution: Map<String, String>
) {

    private val tempDir = System.getProperty("java.io.tmpdir")
    private val tempPath = if (tempDir.endsWith(File.separator)) {
        tempDir + "nimbus" + File.separator
    } else {
        tempDir + File.separator + "nimbus" + File.separator
    }

    fun handleUploadingFile(bucketUploads: Map<Class<*>, List<FileUploadDescription>>) {
        for ((bucketClass, fileUploads) in bucketUploads) {
            val fileStorageClient = ClientBuilder.getFileStorageClient(bucketClass)

            for ((localFile, targetFile, substituteVariablesFileRegex) in fileUploads) {
                val file = File(localFile)
                val compiledPattern = Pattern.compile(substituteVariablesFileRegex)
                val matcher: (String) -> Boolean = if (substituteVariablesFileRegex.isNotEmpty()) {
                    { compiledPattern.matcher(it).matches() }
                } else {
                    { false }
                }

                if (file.isFile) {
                    if (matcher(file.name)) {
                        fileStorageClient.saveFile(targetFile, substituteVariables(file))
                    } else {
                        fileStorageClient.saveFile(targetFile, file)
                    }
                } else if (file.isDirectory){
                    val newPath = if (targetFile.endsWith("/") || targetFile.isEmpty()) {
                        targetFile
                    } else {
                        "$targetFile/"
                    }
                    uploadDirectory(fileStorageClient, file, newPath, matcher)
                } else {
                    throw IllegalArgumentException("$localFile is not file or directory on the system")
                }
            }
        }
    }

    private fun uploadDirectory(fileStorageClient: FileStorageClient, directory: File, s3Path: String, matcher: (String) -> Boolean) {
        for (file in directory.listFiles()) {
            val newPath = if (s3Path.isEmpty()) {
                file.name
            } else {
                "$s3Path/${file.name}"
            }

            if (file.isFile) {
                if (matcher(file.name)) {
                    fileStorageClient.saveFile(newPath, substituteVariables(file))
                } else {
                    fileStorageClient.saveFile(newPath, file)
                }
            } else if (file.isDirectory){
                uploadDirectory(fileStorageClient, file, newPath, matcher)
            }
        }
    }

    private fun substituteVariables(file: File): File {
        val charset = StandardCharsets.UTF_8

        var content = String(file.readBytes(), charset)
        for ((from, to) in variableSubstitution) {
            content = content.replace(from, to)
        }

        val newFile = File(tempPath + file.name)
        newFile.parentFile.mkdirs()
        newFile.writeBytes(content.toByteArray(charset))
        return newFile
    }
}
