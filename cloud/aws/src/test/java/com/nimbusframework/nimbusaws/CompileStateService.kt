package com.nimbusframework.nimbusaws

import com.google.common.collect.ImmutableList
import com.google.testing.compile.Compilation
import com.google.testing.compile.Compiler
import com.google.testing.compile.JavaFileObjects
import com.nimbusframework.nimbusaws.annotation.processor.NimbusAnnotationProcessor
import com.nimbusframework.nimbusaws.annotation.services.FileReader
import io.kotest.matchers.shouldBe
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic
import javax.tools.JavaFileObject

class CompileStateService(
        vararg filesToCompile: String
) {

    private val fileService = FileReader()

    lateinit var status: Compilation.Status
    lateinit var diagnostics: ImmutableList<Diagnostic<out JavaFileObject>>

    private val fileObjects: List<JavaFileObject>

    init {
        fileObjects = filesToCompile.map {
            val fileText = fileService.getResourceFileText(it)
            val fullyQualifiedName = it.replace('/', '.').removeSuffix(".java")
            JavaFileObjects.forSourceString(fullyQualifiedName, fileText)
        }
    }

    fun compileObjectsWithNimbus() {
        val compiler = Compiler.javac()
        val compilation = compiler
            .withProcessors(NimbusAnnotationProcessor())
            .compile(fileObjects)

        status = compilation.status()
        diagnostics = compilation.diagnostics()
    }

    fun compileObjects(toRunWhileProcessing: (ProcessingEnvironment) -> Unit) {
        val compiler = Compiler.javac()

        val compilation = compiler
            .withProcessors(EvaluatingProcessor {
                toRunWhileProcessing(it)
            })
            .compile(fileObjects)

        status = compilation.status()
        diagnostics = compilation.diagnostics()
    }

    internal inner class EvaluatingProcessor(private val toRunWhileProcessing: (ProcessingEnvironment) -> Unit) : AbstractProcessor() {

        override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment): Boolean {
            return true
        }

        @Synchronized
        override fun init(processingEnv: ProcessingEnvironment) {
            super.init(processingEnv)
            toRunWhileProcessing(processingEnv)
        }
    }
}
