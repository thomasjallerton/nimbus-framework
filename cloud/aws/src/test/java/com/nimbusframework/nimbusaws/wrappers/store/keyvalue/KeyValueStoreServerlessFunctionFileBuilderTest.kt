package com.nimbusframework.nimbusaws.wrappers.store.keyvalue

import com.google.testing.compile.Compilation
import com.nimbusframework.nimbusaws.CompileStateService
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe

internal class KeyValueStoreServerlessFunctionFileBuilderTest: AnnotationSpec() {

    @Test
    fun correctCompiles() {
        val compileStateService = CompileStateService("handlers/KeyValueStoreHandlers.java", "models/KeyValue.java", "models/DynamoDbKeyValue.java")
        compileStateService.compileObjectsWithNimbus()

        compileStateService.status shouldBe Compilation.Status.SUCCESS
    }
}
