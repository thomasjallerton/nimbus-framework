package com.nimbusframework.nimbusaws

import com.google.common.collect.ImmutableList
import com.google.testing.compile.Compilation
import com.google.testing.compile.Compiler
import com.google.testing.compile.JavaFileObjects
import com.nimbusframework.nimbusaws.annotation.processor.NimbusAnnotationProcessor
import com.nimbusframework.nimbusaws.annotation.services.FileReader
import io.kotlintest.shouldBe
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic
import javax.tools.JavaFileObject

class CompileStateService(
        vararg filesToCompile: String,
        private val useNimbus: Boolean = false
) {

    private val fileService = FileReader()
    lateinit var elements: Elements
    lateinit var types: Types
    lateinit var processingEnvironment: ProcessingEnvironment

    val status: Compilation.Status
    val diagnostics: ImmutableList<Diagnostic<out JavaFileObject>>

    init {
        val fileObjects = filesToCompile.map {
            val fileText = fileService.getResourceFileText(it)
            val fullyQualifiedName = it.replace('/', '.').removeSuffix(".java")
            JavaFileObjects.forSourceString(fullyQualifiedName, fileText)
        }

        val compiler = Compiler.javac()
        val compilation = if (useNimbus) {
            compiler.withProcessors(NimbusAnnotationProcessor())
        } else {
            compiler.withProcessors(EvaluatingProcessor())
        }.compile(fileObjects)

        status = compilation.status()
        diagnostics = compilation.diagnostics()
    }

    internal inner class EvaluatingProcessor : AbstractProcessor() {

        override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment): Boolean {
            return true
        }

        @Synchronized
        override fun init(processingEnv: ProcessingEnvironment) {
            super.init(processingEnv)
            processingEnvironment = processingEnv
            elements = processingEnv.elementUtils
            types = processingEnv.typeUtils
        }
    }
}