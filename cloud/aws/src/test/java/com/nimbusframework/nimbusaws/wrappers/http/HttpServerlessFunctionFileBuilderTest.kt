package com.nimbusframework.nimbusaws.wrappers.http

import com.google.testing.compile.Compilation
import com.google.testing.compile.Compiler
import com.google.testing.compile.JavaFileObjects
import com.nimbusframework.nimbusaws.annotation.processor.NimbusAnnotationProcessor
import com.nimbusframework.nimbusaws.annotation.services.FileReader
import com.nimbusframework.nimbuscore.annotations.function.HttpRequestPartLog
import com.nimbusframework.nimbuscore.persisted.userconfig.HttpErrorMessageType
import com.nimbusframework.nimbuscore.persisted.userconfig.UserConfig
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe

internal class HttpServerlessFunctionFileBuilderTest: FreeSpec({

    val fileService = FileReader()


    "Correctly compiles http function" - {
        val httpErrorMessageTypes = HttpErrorMessageType.values()
        httpErrorMessageTypes.forEach { type ->
            "error handling type: $type" {
                val fileText = fileService.getResourceFileText("handlers/HttpHandlers.java")

                val compilation = Compiler.javac().withProcessors(NimbusAnnotationProcessor(UserConfig(httpErrorMessageType = type)))
                    .compile(JavaFileObjects.forSourceString("document.handlers.HttpHandlers", fileText))
                compilation.status() shouldBe Compilation.Status.SUCCESS
            }
        }
    }

    "Correctly compiles http function - log request part" - {
        val httpRequestPart = HttpRequestPartLog.values()
        httpRequestPart.forEach { part ->
            "log request part: $part" {
                val fileText = fileService.getResourceFileText("handlers/HttpHandlers.java")

                val compilation = Compiler.javac().withProcessors(NimbusAnnotationProcessor(UserConfig(httpLogRequestParts = listOf(part))))
                    .compile(JavaFileObjects.forSourceString("document.handlers.HttpHandlers", fileText))
                compilation.status() shouldBe Compilation.Status.SUCCESS
            }
        }
    }


})
