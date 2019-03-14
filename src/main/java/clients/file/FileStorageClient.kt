package clients.file

import java.io.File
import java.io.InputStream

interface FileStorageClient {

    fun saveFile(path: String, file: File)

    fun deleteFile(path: String)

    fun listFiles(): List<FileInformation>

    fun getFile(path: String): InputStream
}