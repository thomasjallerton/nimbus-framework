package com.nimbusframework.nimbuslocal.deployment.file

import com.nimbusframework.nimbuscore.annotations.file.FileStorageEventType
import com.nimbusframework.nimbuslocal.LocalNimbusDeployment
import com.nimbusframework.nimbuslocal.exampleModels.Bucket
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.io.File

internal class LocalFileStorageTest: StringSpec({

    LocalNimbusDeployment.getNewInstance(Bucket::class.java)
    val underTest = LocalFileStorage("Test", listOf("*"))

    "Can detect woff file type" {
        val file = mockk<File>()
        every { file.name } returns "path.woff"
        underTest.determineContentType(file) shouldBe "font/woff"
    }

    "Can save file with tags" {
        underTest.saveFile("this/is/a/file/path", "HELLO WORLD", mapOf(Pair("Hello", "World")))

        val contents = underTest.getFile("this/is/a/file/path").bufferedReader().use { it.readText() }
        contents shouldBe "HELLO WORLD"
        underTest.getFileTags("this/is/a/file/path") shouldBe mapOf(Pair("Hello", "World"))
    }

    "Can save file from InputStream" {
        val inputStream = "INPUT STREAM CONTENT".byteInputStream()
        underTest.saveFile("input/stream/path", inputStream)

        val contents = underTest.getFile("input/stream/path").bufferedReader().use { it.readText() }
        contents shouldBe "INPUT STREAM CONTENT"
    }

    "Can save file from ByteArray" {
        val content = "BYTE ARRAY CONTENT".toByteArray()
        underTest.saveFile("byte/array/path", content)

        val contents = underTest.getFile("byte/array/path").use { it.readAllBytes() }
        contents shouldBe content
    }

    "Can save file from File" {
        val tempFile = File.createTempFile("testFile", ".txt")
        tempFile.writeText("FILE CONTENT")
        underTest.saveFile("file/path", tempFile)

        val contents = underTest.getFile("file/path").bufferedReader().use { it.readText() }
        contents shouldBe "FILE CONTENT"
        tempFile.delete()
    }

    "Can save file with content type" {
        underTest.saveFileWithContentType("content/type/path", "CONTENT", "text/plain")

        val contents = underTest.getFile("content/type/path").bufferedReader().use { it.readText() }
        contents shouldBe "CONTENT"
        // Content type is used by the web handler, but we can't easily check that here without mocking the handler.
        // However, we verify it doesn't crash and saves the file.
    }

    "Can save file from InputStream with content type" {
        val inputStream = "INPUT STREAM WITH CT".byteInputStream()
        underTest.saveFileWithContentType("input/stream/ct/path", inputStream, "application/pdf")

        val contents = underTest.getFile("input/stream/ct/path").bufferedReader().use { it.readText() }
        contents shouldBe "INPUT STREAM WITH CT"
    }

    "Can save file from ByteArray with content type" {
        val content = "BYTE ARRAY WITH CT".toByteArray()
        underTest.saveFileWithContentType("byte/array/ct/path", content, "image/png")

        val contents = underTest.getFile("byte/array/ct/path").use { it.readAllBytes() }
        contents shouldBe content
    }

    "Can save file from File with content type" {
        val tempFile = File.createTempFile("testFileCt", ".txt")
        tempFile.writeText("FILE CONTENT WITH CT")
        underTest.saveFileWithContentType("file/ct/path", tempFile, "text/csv")

        val contents = underTest.getFile("file/ct/path").bufferedReader().use { it.readText() }
        contents shouldBe "FILE CONTENT WITH CT"
        tempFile.delete()
    }

    "Can delete file" {
        underTest.saveFile("delete/me", "DELETE ME")

        underTest.deleteFile("delete/me")

        val fileList = underTest.listFiles()
        fileList.map { it.path } shouldBe emptyList<String>()
    }

    "Can delete multiple files" {
        underTest.saveFile("delete/1", "1")
        underTest.saveFile("delete/2", "2")

        underTest.deleteFiles(listOf("delete/1", "delete/2"))

        val fileList = underTest.listFiles()
        fileList.map { it.path } shouldBe emptyList<String>()
    }

    "Can list files" {
        underTest.deleteFiles(underTest.listFiles().map { it.path }) // Cleanup
        underTest.saveFile("list/1.txt", "1".repeat(1024))
        underTest.saveFile("list/inner/2.txt", "22".repeat(1024))

        val fileList = underTest.listFiles()
        fileList.size shouldBe 2
        val paths = fileList.map { it.path }
        paths shouldContainExactlyInAnyOrder listOf("list/1.txt", "list/inner/2.txt")

        val file1 = fileList.find { it.path == "list/1.txt" }!!
        file1.size shouldBe 1 // "1".repeat(1024) is 1024 bytes, so 1 KB

        val file2 = fileList.find { it.path == "list/inner/2.txt" }!!
        file2.size shouldBe 2 // "22".repeat(1024) is 2048 bytes, so 2 KB
    }

    "Invokes methods on OBJECT_CREATED and OBJECT_DELETED" {
        val method = mockk<FileStorageMethod>(relaxed = true)
        underTest.addMethod(method)

        underTest.saveFile("trigger/created", "CONTENT")
        verify { method.invoke(any(), any(), FileStorageEventType.OBJECT_CREATED) }

        underTest.deleteFile("trigger/created")
        verify { method.invoke("trigger/created", 0, FileStorageEventType.OBJECT_DELETED) }
    }

    afterEach {
        val underTest = LocalFileStorage("Test", listOf("*"))
        val files = underTest.listFiles()
        underTest.deleteFiles(files.map { it.path })
        underTest.listFiles() shouldBe listOf()
    }

})
