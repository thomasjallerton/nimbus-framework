package com.nimbusframework.nimbusaws.wrappers.basic

import com.google.testing.compile.Compilation
import com.google.testing.compile.Compiler.javac
import com.google.testing.compile.JavaFileObjects
import com.nimbusframework.nimbusaws.annotation.processor.NimbusAnnotationProcessor
import com.nimbusframework.nimbusaws.annotation.services.FileReader
import io.kotlintest.shouldBe
import io.kotlintest.specs.AnnotationSpec

internal class BasicServerlessFunctionFileBuilderTest: AnnotationSpec() {

    private val fileService = FileReader()

    @Test
    fun correctCompiles() {
        val fileText = fileService.getResourceFileText("handlers/BasicHandlers.java")

        val compilation = javac().withProcessors(NimbusAnnotationProcessor()).compile(JavaFileObjects.forSourceString("handlers.BasicHandlers", fileText))
        compilation.status() shouldBe Compilation.Status.SUCCESS
    }
}