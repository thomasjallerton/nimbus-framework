package com.nimbusframework.nimbusaws.clients.file

import com.nimbusframework.nimbuscore.clients.file.FileInformation
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.*
import software.amazon.awssdk.core.ResponseInputStream
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.*
import software.amazon.awssdk.utils.Md5Utils
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.time.Instant

class FileStorageClientS3Test : AnnotationSpec() {

    private lateinit var underTest: FileStorageClientS3
    private lateinit var s3Client: S3Client

    private val bucketName = "bucketnamedev"

    @BeforeEach
    fun setup() {
        s3Client = mockk(relaxed = true)
        underTest = FileStorageClientS3("BUCKETNAME", "dev", s3Client)
    }

    @Test
    fun canSaveInputStream() {
        val inputStream: InputStream = "hello world".byteInputStream()

        val putObjectRequest = slot<PutObjectRequest>()
        val requestBody = slot<RequestBody>()

        every { s3Client.putObject(capture(putObjectRequest), capture(requestBody)) } returns PutObjectResponse.builder().build()

        underTest.saveFile("path", inputStream)

        putObjectRequest.captured.key() shouldBe "path"
        putObjectRequest.captured.bucket() shouldBe bucketName
        putObjectRequest.captured.contentMD5() shouldBe "XrY7u+Ae7tCTyyK7j1rNww=="
        requestBody.captured.contentStreamProvider().newStream() shouldBe inputStream
    }

    @Test
    fun canSaveInputStreamWithTags() {
        val inputStream: InputStream = "hello world".byteInputStream()

        val putObjectRequest = slot<PutObjectRequest>()
        val requestBody = slot<RequestBody>()

        every { s3Client.putObject(capture(putObjectRequest), capture(requestBody)) } returns PutObjectResponse.builder().build()

        underTest.saveFile("path", inputStream, mapOf(Pair("Example tag", "value")))

        putObjectRequest.captured.key() shouldBe "path"
        putObjectRequest.captured.bucket() shouldBe bucketName
        putObjectRequest.captured.tagging() shouldBe "Example%20tag=value"
        putObjectRequest.captured.contentMD5() shouldBe "XrY7u+Ae7tCTyyK7j1rNww=="
        requestBody.captured.contentStreamProvider().newStream() shouldBe inputStream
    }

    @Test
    fun canSaveByteArray() {
        val byteArray = ByteArray(20) { it.toByte() }

        val putObjectRequest = slot<PutObjectRequest>()
        val requestBody = slot<RequestBody>()

        every { s3Client.putObject(capture(putObjectRequest), capture(requestBody)) } returns PutObjectResponse.builder().build()

        underTest.saveFile("path", byteArray)

        putObjectRequest.captured.key() shouldBe "path"
        putObjectRequest.captured.bucket() shouldBe bucketName
        putObjectRequest.captured.contentMD5() shouldBe "FUnRquICFOBlq0t2qqyJqA=="
        requestBody.captured.contentStreamProvider().newStream().readAllBytes() shouldBe byteArray
    }

    @Test
    fun canSaveByteArrayWithTags() {
        val byteArray = ByteArray(20) { it.toByte() }

        val putObjectRequest = slot<PutObjectRequest>()
        val requestBody = slot<RequestBody>()

        every { s3Client.putObject(capture(putObjectRequest), capture(requestBody)) } returns PutObjectResponse.builder().build()

        underTest.saveFile("path", byteArray, mapOf(Pair("Example tag", "value")))

        putObjectRequest.captured.key() shouldBe "path"
        putObjectRequest.captured.bucket() shouldBe bucketName
        putObjectRequest.captured.tagging() shouldBe "Example%20tag=value"
        putObjectRequest.captured.contentMD5() shouldBe "FUnRquICFOBlq0t2qqyJqA=="
        requestBody.captured.contentStreamProvider().newStream().readAllBytes() shouldBe byteArray
    }

    @Test
    fun canSaveStringWithContentType() {
        val putObjectRequest = slot<PutObjectRequest>()
        val requestBody = slot<RequestBody>()

        every { s3Client.putObject(capture(putObjectRequest), capture(requestBody)) } returns PutObjectResponse.builder().build()

        underTest.saveFileWithContentType("path", "content", "text/plain")

        putObjectRequest.captured.bucket() shouldBe bucketName
        putObjectRequest.captured.contentMD5() shouldBe "mgNkuembtIDdJeHwKEyFVQ=="
        requestBody.captured.contentStreamProvider().newStream().bufferedReader().use(BufferedReader::readText) shouldBe "content"
        putObjectRequest.captured.contentType() shouldBe "text/plain"
    }

    @Test
    fun canSaveStringWithContentTypeAndTags() {
        val putObjectRequest = slot<PutObjectRequest>()
        val requestBody = slot<RequestBody>()

        every { s3Client.putObject(capture(putObjectRequest), capture(requestBody)) } returns PutObjectResponse.builder().build()

        underTest.saveFileWithContentType("path", "content", "text/plain", mapOf(Pair("Key", "thing")))

        putObjectRequest.captured.bucket() shouldBe bucketName
        putObjectRequest.captured.tagging() shouldBe "Key=thing"
        putObjectRequest.captured.contentMD5() shouldBe "mgNkuembtIDdJeHwKEyFVQ=="
        requestBody.captured.contentStreamProvider().newStream().bufferedReader().use(BufferedReader::readText) shouldBe "content"
        putObjectRequest.captured.contentType() shouldBe "text/plain"
    }

    @Test
    fun canSaveFileWithContentType() {
        val putObjectRequest = slot<PutObjectRequest>()
        val requestBody = slot<RequestBody>()

        every { s3Client.putObject(capture(putObjectRequest), capture(requestBody)) } returns PutObjectResponse.builder().build()

        underTest.saveFileWithContentType("path", File.createTempFile("test", "test"), "text/plain")

        putObjectRequest.captured.bucket() shouldBe bucketName
        requestBody.captured.contentStreamProvider().newStream().bufferedReader().use(BufferedReader::readText) shouldBe ""
        putObjectRequest.captured.contentType() shouldBe "text/plain"
    }

    @Test
    fun canSaveFileWithContentTypeAndTags() {
        val putObjectRequest = slot<PutObjectRequest>()
        val requestBody = slot<RequestBody>()

        every { s3Client.putObject(capture(putObjectRequest), capture(requestBody)) } returns PutObjectResponse.builder().build()

        underTest.saveFileWithContentType("path", File.createTempFile("test", "test"), "text/plain", mapOf(Pair("Other", "cat")))

        putObjectRequest.captured.bucket() shouldBe bucketName
        putObjectRequest.captured.tagging() shouldBe "Other=cat"
        requestBody.captured.contentStreamProvider().newStream().bufferedReader().use(BufferedReader::readText) shouldBe ""
        putObjectRequest.captured.contentType() shouldBe "text/plain"
    }

    @Test
    fun canSaveInputStreamWithContentType() {
        val inputStream: InputStream = "hello world".byteInputStream()
        val putObjectRequest = slot<PutObjectRequest>()
        val requestBody = slot<RequestBody>()

        every { s3Client.putObject(capture(putObjectRequest), capture(requestBody)) } returns PutObjectResponse.builder().build()

        underTest.saveFileWithContentType("path", inputStream, "text/plain")

        putObjectRequest.captured.key() shouldBe "path"
        putObjectRequest.captured.bucket() shouldBe bucketName
        putObjectRequest.captured.contentMD5() shouldBe "XrY7u+Ae7tCTyyK7j1rNww=="
        requestBody.captured.contentStreamProvider().newStream() shouldBe inputStream
        requestBody.captured.contentType() shouldBe "text/plain"
    }

    @Test
    fun canSaveInputStreamWithContentTypeAndTags() {
        val inputStream: InputStream = "hello world".byteInputStream()
        val putObjectRequest = slot<PutObjectRequest>()
        val requestBody = slot<RequestBody>()

        every { s3Client.putObject(capture(putObjectRequest), capture(requestBody)) } returns PutObjectResponse.builder().build()

        underTest.saveFileWithContentType("path", inputStream, "text/plain", mapOf(Pair("key", "dog")))

        putObjectRequest.captured.key() shouldBe "path"
        putObjectRequest.captured.bucket() shouldBe bucketName
        putObjectRequest.captured.tagging() shouldBe "key=dog"
        putObjectRequest.captured.contentMD5() shouldBe "XrY7u+Ae7tCTyyK7j1rNww=="
        requestBody.captured.contentStreamProvider().newStream() shouldBe inputStream
        requestBody.captured.contentType() shouldBe "text/plain"
    }

    @Test
    fun canGetFile() {
        val inputStream: ResponseInputStream<GetObjectResponse> = mockk()
        val getObjectRequest = slot<GetObjectRequest>()

        every { s3Client.getObject(capture(getObjectRequest)) } returns inputStream

        underTest.getFile("file") shouldBe inputStream
    }

    @Test
    fun canListFiles() {
        val listObjectsRequest = slot<ListObjectsRequest>()

        val time = Instant.now()

        val firstObject: S3Object = mockk()
        val secondObject: S3Object = mockk()

        val listObjectsResponse: ListObjectsResponse = mockk()

        every { s3Client.listObjects(capture(listObjectsRequest)) } returns listObjectsResponse
        every { listObjectsResponse.contents() } returns listOf(firstObject, secondObject)

        every { firstObject.lastModified() } returns time
        every { firstObject.size() } returns 100
        every { firstObject.key() } returns "file1"

        every { secondObject.lastModified() } returns time
        every { secondObject.size() } returns 101
        every { secondObject.key() } returns "file2"

        underTest.listFiles() shouldContainExactlyInAnyOrder listOf(
                FileInformation(time, 100, "file1"),
                FileInformation(time, 101, "file2"))
    }

    @Test
    fun canSaveFile() {
        val file: File = mockk(relaxed = true)
        val putObjectRequest = slot<PutObjectRequest>()
        val requestBody = slot<RequestBody>()
        mockkStatic(Md5Utils::class)

        every { s3Client.putObject(capture(putObjectRequest), capture(requestBody)) } returns PutObjectResponse.builder().build()
        every { Md5Utils.md5AsBase64(file) } returns "HASH"
        underTest.saveFile("path", file)

        putObjectRequest.captured.bucket() shouldBe bucketName
        putObjectRequest.captured.key() shouldBe "path"
        putObjectRequest.captured.contentMD5() shouldBe "HASH"
        requestBody.captured.contentType() shouldBe "application/octet-stream"
        unmockkStatic(Md5Utils::class)
    }

    @Test
    fun canSaveFileWithTags() {
        val file: File = mockk(relaxed = true)
        val putObjectRequest = slot<PutObjectRequest>()
        val requestBody = slot<RequestBody>()
        mockkStatic(Md5Utils::class)

        every { s3Client.putObject(capture(putObjectRequest), capture(requestBody)) } returns PutObjectResponse.builder().build()
        every { Md5Utils.md5AsBase64(file) } returns "HASH"

        underTest.saveFile("path", file, mapOf(Pair("booo", "ahhh")))

        putObjectRequest.captured.bucket() shouldBe bucketName
        putObjectRequest.captured.key() shouldBe "path"
        putObjectRequest.captured.tagging() shouldBe "booo=ahhh"
        putObjectRequest.captured.contentMD5() shouldBe "HASH"
        requestBody.captured.contentType() shouldBe "application/octet-stream"
        unmockkStatic(Md5Utils::class)
    }

    @Test
    fun canSaveString() {
        val putObjectRequest = slot<PutObjectRequest>()
        val requestBody = slot<RequestBody>()

        every { s3Client.putObject(capture(putObjectRequest), capture(requestBody)) } returns PutObjectResponse.builder().build()

        underTest.saveFile("path", "test")

        putObjectRequest.captured.bucket() shouldBe bucketName
        putObjectRequest.captured.key() shouldBe "path"
        requestBody.captured.contentStreamProvider().newStream().bufferedReader().use(BufferedReader::readText) shouldBe "test"
        requestBody.captured.contentType() shouldContain "text/plain"
    }

    @Test
    fun canSaveStringAndTags() {
        val putObjectRequest = slot<PutObjectRequest>()
        val requestBody = slot<RequestBody>()

        every { s3Client.putObject(capture(putObjectRequest), capture(requestBody)) } returns PutObjectResponse.builder().build()

        underTest.saveFile("path", "test", mapOf(Pair("test", "")))

        putObjectRequest.captured.bucket() shouldBe bucketName
        putObjectRequest.captured.key() shouldBe "path"
        putObjectRequest.captured.tagging() shouldBe "test="
        requestBody.captured.contentStreamProvider().newStream().bufferedReader().use(BufferedReader::readText) shouldBe "test"
        requestBody.captured.contentType() shouldContain "text/plain"
    }

    @Test
    fun canDeleteFile() {
        val deleteObjectRequest = slot<DeleteObjectRequest>()
        every { s3Client.deleteObject(capture(deleteObjectRequest)) } returns mockk()

        underTest.deleteFile("path")

        deleteObjectRequest.captured.bucket() shouldBe bucketName
        deleteObjectRequest.captured.key() shouldBe "path"
    }
}
