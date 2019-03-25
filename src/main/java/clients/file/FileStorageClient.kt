package clients.file

import java.io.File
import java.io.InputStream

interface FileStorageClient {

    fun saveFile(path: String, file: File)

    fun saveFile(path: String, content: String)

    fun saveHtmlFile(path: String, content: String)

    fun deleteFile(path: String)

    fun listFiles(): List<FileInformation>

    fun getFile(path: String): InputStream

}
