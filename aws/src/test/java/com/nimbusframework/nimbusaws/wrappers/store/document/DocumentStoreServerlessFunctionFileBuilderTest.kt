package com.nimbusframework.nimbusaws.wrappers.store.document

import com.google.testing.compile.Compilation
import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.annotation.services.FileReader
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.AnnotationSpec
import javax.tools.Diagnostic
import javax.tools.JavaFileObject

internal class DocumentStoreServerlessFunctionFileBuilderTest: AnnotationSpec() {

    @Test
    fun correctCompiles() {
        val compilation = CompileStateService("handlers/DocumentStoreHandlers.java", "models/Document.java", "models/DynamoDbDocument.java", "models/DocumentExistingArn.java", useNimbus = true)
        compilation.status shouldBe Compilation.Status.SUCCESS
    }

    @Test
    fun insertTwoEventArgumentsFailsCompilation() {
        val compilation = CompileStateService("handlers/BadDocumentStoreHandlers.java", "models/Document.java", "models/DynamoDbDocument.java", "models/DocumentExistingArn.java", useNimbus = true)

        compilation.status shouldBe Compilation.Status.FAILURE
        compilation.diagnostics shouldNotBe emptyList<Diagnostic<out JavaFileObject>>()
    }
}