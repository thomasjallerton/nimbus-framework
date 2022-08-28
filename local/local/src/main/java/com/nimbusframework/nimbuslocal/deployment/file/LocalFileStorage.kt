package com.nimbusframework.nimbuslocal.deployment.file

import com.nimbusframework.nimbuscore.annotations.file.FileStorageEventType
import com.nimbusframework.nimbuscore.clients.file.FileInformation
import com.nimbusframework.nimbuscore.clients.file.FileStorageClient
import com.nimbusframework.nimbuslocal.LocalNimbusDeployment
import com.nimbusframework.nimbuslocal.deployment.webserver.WebServerHandler
import org.apache.tika.Tika
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URLConnection
import java.time.Instant
import java.util.*


class LocalFileStorage(bucketName: String, private val allowedOrigins: List<String>) : FileStorageClient {

    private val localNimbusClient = LocalNimbusDeployment.getInstance()
    private val handler: WebServerHandler? = localNimbusClient.getLocalHandler(bucketName)
    private val tmpDir: String
    private val methods: MutableList<FileStorageMethod> = mutableListOf()
    private val fileTags: MutableMap<String, Map<String, String>> = mutableMapOf()

    init {
        System.setProperty("content.types.user.table","mimetypes/content-types.properties")
    }

    override fun saveFile(path: String, inputStream: InputStream, tags: Map<String, String>) {
        val outputFile = saveInputStreamToFile(path, inputStream)
        fileTags[path] = tags
        addNewWebHandler(path, outputFile, determineContentType(outputFile))
    }

    override fun saveFile(path: String, file: File, tags: Map<String, String>) {
        val outputFile = saveFileToFile(path, file)
        fileTags[path] = tags
        addNewWebHandler(path, outputFile, determineContentType(file))
    }

    override fun saveFile(path: String, content: String, tags: Map<String, String>) {
        val outputFile = saveStringToFile(path, content)
        fileTags[path] = tags
        addNewWebHandler(path, outputFile, determineContentType(outputFile))
    }

    override fun saveFileWithContentType(path: String, content: String, contentType: String, tags: Map<String, String>) {
        val outputFile = saveStringToFile(path, content)
        fileTags[path] = tags
        addNewWebHandler(path, outputFile, contentType)
    }

    override fun saveFileWithContentType(path: String, file: File, contentType: String, tags: Map<String, String>) {
        val outputFile = saveFileToFile(path, file)
        fileTags[path] = tags
        addNewWebHandler(path, outputFile, contentType)
    }

    override fun saveFileWithContentType(path: String, inputStream: InputStream, contentType: String, tags: Map<String, String>) {
        val outputFile = saveInputStreamToFile(path, inputStream)
        fileTags[path] = tags
        addNewWebHandler(path, outputFile, contentType)
    }

    fun getFileTags(path: String): Map<String, String>? {
        return fileTags[path]
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
        fileTags.remove(path)
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
                    val lastModified = Instant.ofEpochMilli(file.lastModified())

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

    fun determineContentType(file: File): String {
        val mimeType: String? = MimeTypeDetector.guessMimeTypeByExtension(file.name)
        return mimeType ?: Tika().detect(file)
    }
}
