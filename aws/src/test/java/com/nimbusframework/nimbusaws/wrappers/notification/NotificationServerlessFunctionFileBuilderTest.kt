package com.nimbusframework.nimbusaws.wrappers.notification

import com.google.testing.compile.Compilation
import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.annotation.services.FileReader
import io.kotlintest.shouldBe
import io.kotlintest.specs.AnnotationSpec

internal class NotificationServerlessFunctionFileBuilderTest: AnnotationSpec() {

    private val fileService = FileReader()

    @Test
    fun correctCompiles() {
        val compileStateService = CompileStateService("models/NotificationTopic.java", "handlers/NotificationHandlers.java", useNimbus = true)
        compileStateService.compileObjects { }

        compileStateService.status shouldBe Compilation.Status.SUCCESS
    }
}