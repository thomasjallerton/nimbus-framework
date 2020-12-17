package com.nimbusframework.nimbusaws.wrappers.notification

import com.google.testing.compile.Compilation
import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.annotation.services.FileReader
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe

internal class NotificationServerlessFunctionFileBuilderTest: AnnotationSpec() {

    private val fileService = FileReader()

    @Test
    fun correctCompiles() {
        val compileStateService = CompileStateService("models/NotificationTopic.java", "handlers/NotificationHandlers.java", useNimbus = true)
        compileStateService.compileObjects { }

        compileStateService.status shouldBe Compilation.Status.SUCCESS
    }
}