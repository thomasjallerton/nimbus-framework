package com.nimbusframework.nimbusaws.clients.file

import com.nimbusframework.nimbuscore.clients.file.FileInformation
import com.nimbusframework.nimbuscore.clients.file.FileStorageClient
import software.amazon.awssdk.core.internal.util.Mimetype
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.*
import java.io.File
import java.io.InputStream
import java.math.BigInteger
import java.nio.charset.Charset
import java.security.MessageDigest
import java.util.*

internal class FileStorageClientS3(
    annotationBucketName: String,
    stage: String,
    private val s3Client: S3Client
): FileStorageClient {

    private val bucketName = (annotationBucketName + stage).lowercase(Locale.getDefault())
    private val md5Checksum = MessageDigest.getInstance("MD5")

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
            .contentType(contentType)
            .contentMD5(md5(content.toByteArray()))
            .key(path)
        if (tags.isNotEmpty()) {
            putObjectRequest.tagging(buildTags(tags))
        }
        val requestBody = RequestBody.fromString(content)
        s3Client.putObject(putObjectRequest.build(), requestBody)
    }

    override fun saveFileWithContentType(path: String, file: File, contentType: String, tags: Map<String, String>) {
        val putObjectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .contentType(contentType)
            .key(path)
        if (tags.isNotEmpty()) {
            putObjectRequest.tagging(buildTags(tags))
        }
        val requestBody = RequestBody.fromFile(file)
        s3Client.putObject(putObjectRequest.build(), requestBody)
    }

    override fun saveFileWithContentType(path: String, inputStream: InputStream, contentType: String, tags: Map<String, String>) {
        val putObjectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .contentType(contentType)
            .key(path)
        if (tags.isNotEmpty()) {
            putObjectRequest.tagging(buildTags(tags))
        }
        val requestBody = RequestBody.fromContentProvider({ inputStream }, contentType)

        s3Client.putObject(putObjectRequest.build(), requestBody)
    }

    override fun saveFileWithContentType(path: String, content: ByteArray, contentType: String, tags: Map<String, String>) {
        val putObjectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .contentType(contentType)
            .contentMD5(md5(content))
            .key(path)
        if (tags.isNotEmpty()) {
            putObjectRequest.tagging(buildTags(tags))
        }
        val requestBody = RequestBody.fromBytes(content)

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
            .contentMD5(md5(content.toByteArray()))
            .key(path)
        if (tags.isNotEmpty()) {
            putObjectRequest.tagging(buildTags(tags))
        }
        val requestBody = RequestBody.fromString(content, Charset.forName("UTF-8"))
        s3Client.putObject(putObjectRequest.build(), requestBody)
    }

    override fun saveFile(path: String, content: ByteArray, tags: Map<String, String>) {
        val putObjectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .contentMD5(md5(content))
            .key(path)
        if (tags.isNotEmpty()) {
            putObjectRequest.tagging(buildTags(tags))
        }
        val requestBody = RequestBody.fromBytes(content)
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

    private fun md5(input: ByteArray): String {
        return BigInteger(1, md5Checksum.digest(input)).toString(16).padStart(32, '0')
    }
}
