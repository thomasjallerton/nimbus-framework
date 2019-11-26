package com.nimbusframework.nimbusaws.clients.file

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import com.google.inject.Inject
import com.nimbusframework.nimbuscore.clients.file.FileInformation
import com.nimbusframework.nimbuscore.clients.file.FileStorageClient
import java.io.File
import java.io.InputStream

internal class FileStorageClientS3(annotationBucketName: String, stage: String): FileStorageClient {

    private val bucketName = (annotationBucketName + stage).toLowerCase()

    @Inject
    private lateinit var s3Client: AmazonS3

    override fun saveFile(path: String, inputStream: InputStream) {
        s3Client.putObject(bucketName, path, inputStream, ObjectMetadata())
    }

    override fun saveFileWithContentType(path: String, content: String, contentType: String) {
        val objectMetadata = ObjectMetadata()
        objectMetadata.contentType = contentType
        val putObjectRequest = PutObjectRequest(bucketName, path, content.byteInputStream(), objectMetadata)

        s3Client.putObject(putObjectRequest)
    }

    override fun saveFileWithContentType(path: String, file: File, contentType: String) {
        val objectMetadata = ObjectMetadata()
        objectMetadata.contentType = contentType
        val putObjectRequest = PutObjectRequest(bucketName, path, file.inputStream(), objectMetadata)

        s3Client.putObject(putObjectRequest)
    }

    override fun saveFileWithContentType(path: String, inputStream: InputStream, contentType: String) {
        val objectMetadata = ObjectMetadata()
        objectMetadata.contentType = contentType
        val putObjectRequest = PutObjectRequest(bucketName, path, inputStream, objectMetadata)

        s3Client.putObject(putObjectRequest)     }


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

    override fun saveFile(path: String, content: String) {
        s3Client.putObject(bucketName, path, content)
    }

    override fun deleteFile(path: String) {
        s3Client.deleteObject(bucketName, path)
    }
}