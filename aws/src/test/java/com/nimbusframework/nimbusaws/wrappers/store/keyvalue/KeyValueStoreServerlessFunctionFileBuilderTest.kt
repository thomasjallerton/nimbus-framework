package com.nimbusframework.nimbusaws.wrappers.store.keyvalue

import com.google.testing.compile.Compilation
import com.google.testing.compile.Compiler.javac
import com.google.testing.compile.JavaFileObjects
import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.annotation.processor.NimbusAnnotationProcessor
import com.nimbusframework.nimbusaws.annotation.services.FileReader
import io.kotlintest.shouldBe
import io.kotlintest.specs.AnnotationSpec

internal class KeyValueStoreServerlessFunctionFileBuilderTest: AnnotationSpec() {

    @Test
    fun correctCompiles() {
        val compilation = CompileStateService("handlers/KeyValueStoreHandlers.java", "models/KeyValue.java", "models/DynamoDbKeyValue.java", "models/KeyValueExistingArn.java", useNimbus = true)

        compilation.status shouldBe Compilation.Status.SUCCESS
    }
}