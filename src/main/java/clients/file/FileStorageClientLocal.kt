package clients.file

import testing.LocalNimbusDeployment
import java.io.File
import java.io.InputStream

class FileStorageClientLocal(bucketName: String): FileStorageClient {

    private val localNimbusClient = LocalNimbusDeployment.getInstance()
    private val fileStorage = localNimbusClient.getLocalFileStorage(bucketName)

    override fun saveFile(path: String, file: File) {
        fileStorage.saveFile(path, file)
    }

    override fun saveFile(path: String, content: String) {
        fileStorage.saveFile(path, content)
    }

    override fun saveHtmlFile(path: String, content: String) {
        fileStorage.saveHtmlFile(path, content)
    }

    override fun deleteFile(path: String) {
        fileStorage.deleteFile(path)
    }

    override fun listFiles(): List<FileInformation> {
        return fileStorage.listFiles()
    }

    override fun getFile(path: String): InputStream {
        return fileStorage.getFile(path)
    }
}