package com.nimbusframework.nimbusaws.wrappers.store.document

import com.google.testing.compile.Compilation
import com.nimbusframework.nimbusaws.CompileStateService
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import javax.tools.Diagnostic
import javax.tools.JavaFileObject

internal class DocumentStoreServerlessFunctionFileBuilderTest: AnnotationSpec() {

    @Test
    fun correctCompiles() {
        val compileStateService = CompileStateService("handlers/DocumentStoreHandlers.java", "models/Document.java", "models/DynamoDbDocument.java", "models/DocumentExistingArn.java", useNimbus = true)
        compileStateService.compileObjects {}
        compileStateService.status shouldBe Compilation.Status.SUCCESS
    }

    @Test
    fun insertTwoEventArgumentsFailsCompilation() {
        val compileStateService = CompileStateService("handlers/BadDocumentStoreHandlers.java", "models/Document.java", "models/DynamoDbDocument.java", "models/DocumentExistingArn.java", useNimbus = true)
        compileStateService.compileObjects {}

        compileStateService.status shouldBe Compilation.Status.FAILURE
        compileStateService.diagnostics shouldNotBe emptyList<Diagnostic<out JavaFileObject>>()
    }
}