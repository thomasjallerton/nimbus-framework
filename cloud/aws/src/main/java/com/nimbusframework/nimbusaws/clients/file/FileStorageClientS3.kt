package com.nimbusframework.nimbusaws.clients.file

import com.nimbusframework.nimbuscore.clients.file.FileInformation
import com.nimbusframework.nimbuscore.clients.file.FileStorageClient
import software.amazon.awssdk.core.internal.util.Mimetype
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.*
import java.io.File
import java.io.InputStream
import java.util.*

internal class FileStorageClientS3(
    annotationBucketName: String,
    stage: String,
    private val s3Client: S3Client
): FileStorageClient {

    private val bucketName = (annotationBucketName + stage).lowercase(Locale.getDefault())

    override fun saveFile(path: String, inputStream: InputStream, tags: Map<String, String>) {
        val putObjectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(path)
        if (tags.isNotEmpty()) {
            putObjectRequest.tagging(buildTags(tags))
        }
        val requestBody = RequestBody.fromContentProvider({ inputStream }, Mimetype.MIMETYPE_TEXT_PLAIN)
        s3Client.putObject(putObjectRequest.build(), requestBody)
    }

    override fun saveFileWithContentType(path: String, content: String, contentType: String, tags: Map<String, String>) {
        val putObjectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(path)
        if (tags.isNotEmpty()) {
            putObjectRequest.tagging(buildTags(tags))
        }
        val requestBody = RequestBody.fromContentProvider({ content.byteInputStream() }, contentType)
        s3Client.putObject(putObjectRequest.build(), requestBody)
    }

    override fun saveFileWithContentType(path: String, file: File, contentType: String, tags: Map<String, String>) {
        val putObjectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(path)
        if (tags.isNotEmpty()) {
            putObjectRequest.tagging(buildTags(tags))
        }
        val requestBody = RequestBody.fromContentProvider({ file.inputStream() }, contentType)

        s3Client.putObject(putObjectRequest.build(), requestBody)
    }

    override fun saveFileWithContentType(path: String, inputStream: InputStream, contentType: String, tags: Map<String, String>) {
        val putObjectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(path)
        if (tags.isNotEmpty()) {
            putObjectRequest.tagging(buildTags(tags))
        }
        val requestBody = RequestBody.fromContentProvider({ inputStream }, contentType)

        s3Client.putObject(putObjectRequest.build(), requestBody)
    }


    override fun getFile(path: String): InputStream {
        val getObject = GetObjectRequest.builder()
            .bucket(bucketName)
            .key(path)
            .build()
        return s3Client.getObject(getObject)
    }

    override fun listFiles(): List<FileInformation> {
        val listRequest = ListObjectsRequest.builder()
            .bucket(bucketName)
            .build()
        val contents = s3Client.listObjects(listRequest).contents()
        return contents.map { s3ObjectSummary -> FileInformation(s3ObjectSummary.lastModified(), s3ObjectSummary.size(), s3ObjectSummary.key()) }
    }

    override fun saveFile(path: String, file: File, tags: Map<String, String>) {
        val putObjectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(path)
        if (tags.isNotEmpty()) {
            putObjectRequest.tagging(buildTags(tags))
        }
        val requestBody = RequestBody.fromFile(file)

        s3Client.putObject(putObjectRequest.build(), requestBody)
    }

    override fun saveFile(path: String, content: String, tags: Map<String, String>) {
        val putObjectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(path)
        if (tags.isNotEmpty()) {
            putObjectRequest.tagging(buildTags(tags))
        }
        val requestBody = RequestBody.fromString(content)
        s3Client.putObject(putObjectRequest.build(), requestBody)
    }

    override fun deleteFile(path: String) {
        val deleteRequest = DeleteObjectRequest.builder()
            .bucket(bucketName)
            .key(path)
            .build()
        s3Client.deleteObject(deleteRequest)
    }

    private fun buildTags(tags: Map<String, String>): Tagging {
        return Tagging.builder().tagSet(tags.map { Tag.builder().key(it.key).value(it.value).build() }).build()
    }
}
