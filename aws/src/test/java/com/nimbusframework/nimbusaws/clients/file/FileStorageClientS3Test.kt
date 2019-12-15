package com.nimbusframework.nimbusaws.clients.file

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.*
import com.amazonaws.services.sns.AmazonSNS
import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.nimbusframework.nimbuscore.clients.function.EnvironmentVariableClient
import io.kotlintest.shouldBe
import io.kotlintest.specs.AnnotationSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.io.BufferedReader
import java.io.File
import java.io.InputStream

class FileStorageClientS3Test : AnnotationSpec() {

    private lateinit var underTest: FileStorageClientS3
    private lateinit var s3Client: AmazonS3

    private val bucketName = "bucketnamedev"

    @BeforeEach
    fun setup() {
        underTest = FileStorageClientS3("BUCKETNAME", "dev")
        s3Client = mockk(relaxed = true)
        val injector = Guice.createInjector(object: AbstractModule() {
            override fun configure() {
                bind(AmazonS3::class.java).toInstance(s3Client)
            }
        })
        injector.injectMembers(underTest)
    }

    @Test
    fun canSaveInputStream() {
        val inputStream: InputStream = mockk()

        underTest.saveFile("path", inputStream)

        verify(exactly = 1) { s3Client.putObject(bucketName, "path", inputStream, any()) }
    }

    @Test
    fun canSaveStringWithContentType() {
        val putObjectRequest = slot<PutObjectRequest>()

        every { s3Client.putObject(capture(putObjectRequest)) } returns PutObjectResult()

        underTest.saveFileWithContentType("path", "content", "text/plain")

        putObjectRequest.captured.bucketName shouldBe bucketName
        putObjectRequest.captured.inputStream.bufferedReader().use(BufferedReader::readText) shouldBe "content"
        putObjectRequest.captured.metadata.contentType shouldBe "text/plain"
    }

    @Test
    fun canSaveFileWithContentType() {
        val putObjectRequest = slot<PutObjectRequest>()

        every { s3Client.putObject(capture(putObjectRequest)) } returns PutObjectResult()

        underTest.saveFileWithContentType("path", File.createTempFile("test", "test"), "text/plain")

        putObjectRequest.captured.bucketName shouldBe bucketName
        putObjectRequest.captured.inputStream.bufferedReader().use(BufferedReader::readText) shouldBe ""
        putObjectRequest.captured.metadata.contentType shouldBe "text/plain"
    }

    @Test
    fun canSaveInputStreamWithContentType() {
        val inputStream: InputStream = mockk()
        val putObjectRequest = slot<PutObjectRequest>()

        every { s3Client.putObject(capture(putObjectRequest)) } returns PutObjectResult()

        underTest.saveFileWithContentType("path", inputStream, "text/plain")

        putObjectRequest.captured.bucketName shouldBe bucketName
        putObjectRequest.captured.inputStream shouldBe inputStream
        putObjectRequest.captured.metadata.contentType shouldBe "text/plain"
    }

    @Test
    fun canGetFile() {
        val inputStream: S3ObjectInputStream = mockk()
        val mockObj: S3Object = mockk()

        every { s3Client.getObject(bucketName, "file") } returns mockObj
        every { mockObj.objectContent } returns inputStream

        underTest.getFile("file") shouldBe inputStream
    }

    @Test
    fun canSaveFile() {
        val file: File = mockk()

        underTest.saveFile("path", file)

        verify(exactly = 1) { s3Client.putObject(bucketName, "path", file) }
    }

    @Test
    fun canSaveString() {
        underTest.saveFile("path", "test")

        verify(exactly = 1) { s3Client.putObject(bucketName, "path", "test") }
    }

    @Test
    fun canDeleteFile() {
        underTest.deleteFile("path")

        verify(exactly = 1) { s3Client.deleteObject(bucketName, "path") }
    }
}