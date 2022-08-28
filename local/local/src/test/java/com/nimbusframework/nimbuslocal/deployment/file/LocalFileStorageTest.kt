package com.nimbusframework.nimbuslocal.deployment.file

import com.nimbusframework.nimbuslocal.LocalNimbusDeployment
import com.nimbusframework.nimbuslocal.exampleModels.Bucket
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
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

})
