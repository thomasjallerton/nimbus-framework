package com.nimbusframework.nimbusaws.wrappers.queue

import com.google.testing.compile.Compilation
import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.annotation.services.FileReader
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe

internal class QueueServerlessFunctionFileBuilderTest: AnnotationSpec() {

    private val fileService = FileReader()

    @Test
    fun correctCompiles() {
        val compileStateService = CompileStateService("models/Queue.java", "handlers/QueueHandlers.java")
        compileStateService.compileObjectsWithNimbus()
        compileStateService.status shouldBe Compilation.Status.SUCCESS
    }
}
