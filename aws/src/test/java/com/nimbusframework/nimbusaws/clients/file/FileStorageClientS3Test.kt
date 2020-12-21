package com.nimbusframework.nimbusaws.clients.file

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.*
import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.nimbusframework.nimbuscore.clients.file.FileInformation
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.util.*

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
    fun canListFiles() {
        val firstObjectList: ObjectListing = mockk()
        val secondObjectList: ObjectListing = mockk()

        val firstObject: S3ObjectSummary = mockk()
        val secondObject: S3ObjectSummary = mockk()

        val time = Date()
        every { firstObjectList.objectSummaries } returns mutableListOf(firstObject)
        every { firstObjectList.isTruncated } returns true

        every { secondObjectList.objectSummaries } returns mutableListOf(secondObject)
        every { secondObjectList.isTruncated } returns false

        every { firstObject.lastModified } returns time
        every { firstObject.size } returns 100
        every { firstObject.key } returns "file1"

        every { secondObject.lastModified } returns time
        every { secondObject.size } returns 101
        every { secondObject.key } returns "file2"

        every { s3Client.listObjects(bucketName) } returns firstObjectList
        every { s3Client.listNextBatchOfObjects(firstObjectList) } returns secondObjectList

        underTest.listFiles() shouldContainExactlyInAnyOrder listOf(
                FileInformation(time, 100, "file1"),
                FileInformation(time, 101, "file2"))
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