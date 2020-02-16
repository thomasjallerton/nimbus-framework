package com.nimbusframework.nimbusaws.wrappers.queue

import com.google.testing.compile.Compilation
import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.annotation.services.FileReader
import io.kotlintest.shouldBe
import io.kotlintest.specs.AnnotationSpec

internal class QueueServerlessFunctionFileBuilderTest: AnnotationSpec() {

    private val fileService = FileReader()

    @Test
    fun correctCompiles() {
        val compileStateService = CompileStateService("models/Queue.java", "handlers/QueueHandlers.java", useNimbus = true)
        compileStateService.compileObjects {  }
        compileStateService.status shouldBe Compilation.Status.SUCCESS
    }
}