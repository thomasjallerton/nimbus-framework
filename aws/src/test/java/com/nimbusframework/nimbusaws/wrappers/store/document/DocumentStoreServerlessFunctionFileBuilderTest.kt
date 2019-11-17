package com.nimbusframework.nimbusaws.wrappers.store.document

import com.google.testing.compile.Compilation
import com.google.testing.compile.Compiler.javac
import com.google.testing.compile.JavaFileObjects
import com.nimbusframework.nimbusaws.annotation.processor.NimbusAnnotationProcessor
import com.nimbusframework.nimbusaws.annotation.services.FileReader
import io.kotlintest.shouldBe
import io.kotlintest.specs.AnnotationSpec

internal class DocumentStoreServerlessFunctionFileBuilderTest: AnnotationSpec() {

    private val fileService = FileReader()

    @Test
    fun correctCompiles() {
        val fileText = fileService.getResourceFileText("handlers/DocumentStoreHandlers.java")

        val compilation = javac().withProcessors(NimbusAnnotationProcessor())
                .compile(JavaFileObjects.forSourceString("handlers.DocumentStoreHandlers", fileText))
        compilation.status() shouldBe Compilation.Status.SUCCESS
    }

    @Test
    fun insertTwoEventArgumentsFailsCompilation() {
        val fileText = fileService.getResourceFileText("handlers/BadDocumentStoreHandlers.java")

        val compilation = javac().withProcessors(NimbusAnnotationProcessor())
                .compile(JavaFileObjects.forSourceString("handlers.BadDocumentStoreHandlers", fileText))
        compilation.status() shouldBe Compilation.Status.FAILURE
    }
}