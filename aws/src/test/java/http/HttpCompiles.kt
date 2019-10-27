package http

import com.google.testing.compile.Compilation
import com.google.testing.compile.Compiler.javac
import com.google.testing.compile.JavaFileObjects
import com.nimbusframework.nimbusaws.annotation.processor.NimbusAnnotationProcessor
import com.nimbusframework.nimbusaws.annotation.services.FileReader
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class HttpCompiles {

    private val fileService = FileReader()

    @Test
    fun correctCompiles() {
        val fileText = fileService.getResourceFileText("handlers/HttpHandlers.java")

        val compilation = javac().withProcessors(NimbusAnnotationProcessor())
                .compile(JavaFileObjects.forSourceString("document.handlers.HttpHandlers", fileText))
        assertEquals(Compilation.Status.SUCCESS, compilation.status())
    }

}