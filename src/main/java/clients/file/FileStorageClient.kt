package clients.file

import java.io.File
import java.io.InputStream

interface FileStorageClient {

    fun saveFile(path: String, file: File)

    fun saveFile(path: String, inputStream: InputStream)

    fun saveFile(path: String, content: String)

    fun saveFileWithContentType(path: String, content: String, contentType: String)

    fun saveFileWithContentType(path: String, file: File, contentType: String)

    fun saveFileWithContentType(path: String, inputStream: InputStream, contentType: String)

    fun deleteFile(path: String)

    fun listFiles(): List<FileInformation>

    fun getFile(path: String): InputStream

}
