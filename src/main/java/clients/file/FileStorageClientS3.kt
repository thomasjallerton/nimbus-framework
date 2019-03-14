package clients.file

import com.amazonaws.services.s3.AmazonS3ClientBuilder
import java.io.File
import java.io.InputStream

internal class FileStorageClientS3(bucketName: String): FileStorageClient {

    private val bucketName = bucketName.toLowerCase()
    private val s3Client = AmazonS3ClientBuilder.defaultClient()

    override fun getFile(path: String): InputStream {
        val result = s3Client.getObject(bucketName, path)
        return result.objectContent
    }

    override fun listFiles(): List<FileInformation> {
        var current = s3Client.listObjects(bucketName)
        val keyList = current.objectSummaries
        current = s3Client.listNextBatchOfObjects(current)

        while (current.isTruncated) {
            keyList.addAll(current.objectSummaries)
            current = s3Client.listNextBatchOfObjects(current)
        }
        keyList.addAll(current.objectSummaries)

        return keyList.map { s3ObjectSummary -> FileInformation(s3ObjectSummary.lastModified, s3ObjectSummary.size, s3ObjectSummary.key) }
    }


    override fun saveFile(path: String, file: File) {
        s3Client.putObject(bucketName, path, file)
    }

    override fun deleteFile(path: String) {
        s3Client.deleteObject(bucketName, path)
    }

}