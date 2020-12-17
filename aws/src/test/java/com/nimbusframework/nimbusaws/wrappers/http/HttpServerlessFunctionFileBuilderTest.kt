package com.nimbusframework.nimbusaws.wrappers.http

import com.google.testing.compile.Compilation
import com.google.testing.compile.Compiler
import com.google.testing.compile.JavaFileObjects
import com.nimbusframework.nimbusaws.annotation.processor.NimbusAnnotationProcessor
import com.nimbusframework.nimbusaws.annotation.services.FileReader
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe

internal class HttpServerlessFunctionFileBuilderTest: AnnotationSpec() {

    private val fileService = FileReader()

    @Test
    fun correctCompiles() {
        val fileText = fileService.getResourceFileText("handlers/HttpHandlers.java")

        val compilation = Compiler.javac().withProcessors(NimbusAnnotationProcessor())
                .compile(JavaFileObjects.forSourceString("document.handlers.HttpHandlers", fileText))
        compilation.status() shouldBe Compilation.Status.SUCCESS
    }
}