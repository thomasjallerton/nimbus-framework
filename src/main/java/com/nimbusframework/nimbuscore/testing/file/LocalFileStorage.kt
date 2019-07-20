package com.nimbusframework.nimbuscore.testing.file

import com.google.common.io.ByteStreams
import com.nimbusframework.nimbuscore.annotation.annotations.file.FileStorageEventType
import com.nimbusframework.nimbuscore.clients.file.FileInformation
import com.nimbusframework.nimbuscore.clients.file.FileStorageClient
import com.nimbusframework.nimbuscore.testing.LocalNimbusDeployment
import com.nimbusframework.nimbuscore.testing.webserver.WebserverHandler
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.*


class LocalFileStorage(bucketName: String, private val allowedOrigins: List<String>) : FileStorageClient {

    private val localNimbusClient = LocalNimbusDeployment.getInstance()
    private val handler: WebserverHandler? = localNimbusClient.getLocalHandler(bucketName)
    private val tmpDir: String
    private val methods: MutableList<FileStorageMethod> = mutableListOf()

    val configuredAsStaticWebsite = handler != null

    override fun saveFile(path: String, inputStream: InputStream) {
        val outputFile = saveInputStreamToFile(path, inputStream)
        addNewWebHandler(path, outputFile)
    }

    override fun saveFile(path: String, file: File) {
        val outputFile = saveFileToFile(path, file)
        addNewWebHandler(path, outputFile, determineContentType(file))
    }

    override fun saveFile(path: String, content: String) {
        val outputFile = saveStringToFile(path, content)
        addNewWebHandler(path, outputFile)
    }

    override fun saveFileWithContentType(path: String, content: String, contentType: String) {
        val outputFile = saveStringToFile(path, content)
        addNewWebHandler(path, outputFile, contentType)
    }

    override fun saveFileWithContentType(path: String, file: File, contentType: String) {
        val outputFile = saveFileToFile(path, file)
        addNewWebHandler(path, outputFile, contentType)
    }

    override fun saveFileWithContentType(path: String, inputStream: InputStream, contentType: String) {
        val outputFile = saveInputStreamToFile(path, inputStream)
        addNewWebHandler(path, outputFile, contentType)
    }

    init {
        val osTempDir = System.getProperty("java.io.tmpdir")
        tmpDir = if (osTempDir.endsWith(File.separator)) {
            System.getProperty("java.io.tmpdir") + "nimbus" + File.separator + bucketName
        } else {
            System.getProperty("java.io.tmpdir") + File.separator + "nimbus" + File.separator + bucketName
        }
        val tmpDirFile = File(tmpDir)
        if (tmpDirFile.exists()) tmpDirFile.deleteRecursively()
    }

    internal fun addMethod(fileStorageMethod: FileStorageMethod) {
        methods.add(fileStorageMethod)
    }

    override fun deleteFile(path: String) {
        val actualPath = tmpDir + File.separator + path
        val f = File(actualPath)
        if (f.exists()) {
            f.delete()
            methods.forEach { method -> method.invoke(path, 0, FileStorageEventType.OBJECT_DELETED) }
        }
    }

    override fun listFiles(): List<FileInformation> {
        val results: MutableList<FileInformation> = mutableListOf()
        listFiles(tmpDir, results)
        return results
    }

    private fun listFiles(directory: String, results: MutableList<FileInformation>) {
        val root = File(directory)

        // Get all files from a directory.
        val fList = root.listFiles()
        if (fList != null) {
            for (file in fList) {
                if (file.isFile) {
                    val fileName = file.absolutePath
                    val path = fileName.removePrefix(tmpDir)
                            .removePrefix(File.separator)
                            .replace(File.separatorChar, '/')
                    val lastModified = Date(file.lastModified())

                    results.add(FileInformation(lastModified, file.length() / 1024, path)) //Convert to KB
                } else if (file.isDirectory) {
                    listFiles(file.absolutePath, results)
                }
            }
        }
    }


    override fun getFile(path: String): InputStream {
        val actualPath = tmpDir + File.separator + path
        val f = File(actualPath)

        return FileInputStream(f)
    }

    private fun addNewWebHandler(path: String, file: File, contentType: String = "text/html") {
        handler?.addWebResource(path, file, contentType, allowedOrigins)
    }

    private fun saveFileToFile(path: String, file: File): File {
        val actualPath = tmpDir + File.separator + path
        val f = File(actualPath)
        f.parentFile?.mkdirs()
        f.createNewFile()
        val initialStream = FileInputStream(file)
        val buffer = ByteArray(initialStream.available())
        initialStream.read(buffer)

        val outStream = FileOutputStream(f)
        outStream.write(buffer)

        outStream.close()

        methods.forEach { method -> method.invoke(actualPath, f.length(), FileStorageEventType.OBJECT_CREATED) }

        return f
    }

    private fun saveInputStreamToFile(path: String, inputStream: InputStream): File {
        val actualPath = tmpDir + File.separator + path
        val f = File(actualPath)
        f.parentFile?.mkdirs()
        f.createNewFile()

        val outStream = FileOutputStream(f)
        inputStream.copyTo(outStream)
        outStream.close()
        inputStream.close()

        methods.forEach { method -> method.invoke(actualPath, f.length(), FileStorageEventType.OBJECT_CREATED) }

        return f
    }

    private fun saveStringToFile(path: String, content: String): File {
        val actualPath = tmpDir + File.separator + path
        val f = File(actualPath)
        f.parentFile?.mkdirs()
        f.createNewFile()

        f.writeText(content)

        methods.forEach { method -> method.invoke(path, f.length(), FileStorageEventType.OBJECT_CREATED) }

        return f
    }

    private fun determineContentType(file: File): String {
        return Files.probeContentType(file.toPath())
    }
}