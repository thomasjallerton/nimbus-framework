package com.nimbusframework.nimbusaws

import com.google.testing.compile.Compilation
import com.google.testing.compile.Compiler
import com.google.testing.compile.JavaFileObjects
import com.nimbusframework.nimbusaws.annotation.services.FileReader
import io.kotlintest.shouldBe
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

class CompileStateService(
        vararg filesToCompile: String
) {

    private val fileService = FileReader()
    lateinit var elements: Elements
    lateinit var types: Types
    lateinit var processingEnvironment: ProcessingEnvironment

    init {
        val fileObjects = filesToCompile.map {
            val fileText = fileService.getResourceFileText(it)
            val fullyQualifiedName = it.replace('/', '.').removeSuffix(".java")
            JavaFileObjects.forSourceString(fullyQualifiedName, fileText)
        }
        val evaluatingProcessor = EvaluatingProcessor()
        val compilation = Compiler.javac().withProcessors(evaluatingProcessor).compile(fileObjects)
        compilation.status() shouldBe Compilation.Status.SUCCESS
    }

    internal inner class EvaluatingProcessor() : AbstractProcessor() {

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