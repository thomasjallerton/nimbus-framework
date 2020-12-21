package com.nimbusframework.nimbuscore.clients.store.conditions.variable

import io.kotlintest.specs.AnnotationSpec
import io.kotlintest.shouldBe

class ColumnVariableTest : AnnotationSpec() {

    @Test
    fun getsColumnName() {
        val columnVariable = ColumnVariable("testColumn")
        columnVariable.getValue() shouldBe "testColumn"
    }

}