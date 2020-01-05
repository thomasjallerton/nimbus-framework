package com.nimbusframework.nimbusaws.wrappers.file

import com.google.testing.compile.Compilation
import com.google.testing.compile.Compiler.javac
import com.google.testing.compile.JavaFileObjects
import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.annotation.processor.NimbusAnnotationProcessor
import com.nimbusframework.nimbusaws.annotation.services.FileReader
import io.kotlintest.shouldBe
import io.kotlintest.specs.AnnotationSpec

internal class FileStorageServerlessFunctionFileBuilderTest: AnnotationSpec() {

    private val fileService = FileReader()

    @Test
    fun correctCompiles() {
        val compileStateService = CompileStateService("models/FileStorage.java", "handlers/FileStorageHandlers.java")
        compileStateService.compileObjects {  }
        compileStateService.status shouldBe Compilation.Status.SUCCESS
    }
}