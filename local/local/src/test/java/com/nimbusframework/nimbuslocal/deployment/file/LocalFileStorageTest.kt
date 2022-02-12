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

})
