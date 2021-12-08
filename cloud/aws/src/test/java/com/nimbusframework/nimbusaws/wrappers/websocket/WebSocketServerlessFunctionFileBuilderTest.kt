package com.nimbusframework.nimbusaws.wrappers.websocket

import com.google.testing.compile.Compilation
import com.google.testing.compile.Compiler.javac
import com.google.testing.compile.JavaFileObjects
import com.nimbusframework.nimbusaws.annotation.processor.NimbusAnnotationProcessor
import com.nimbusframework.nimbusaws.annotation.services.FileReader
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe

internal class WebSocketServerlessFunctionFileBuilderTest: AnnotationSpec() {

    private val fileService = FileReader()

    @Test
    fun correctCompiles() {
        val fileText = fileService.getResourceFileText("handlers/WebSocketHandlers.java")

        val compilation = javac().withProcessors(NimbusAnnotationProcessor())
                .compile(JavaFileObjects.forSourceString("handlers.WebSocketHandlers", fileText))
        compilation.status() shouldBe Compilation.Status.SUCCESS
    }
}