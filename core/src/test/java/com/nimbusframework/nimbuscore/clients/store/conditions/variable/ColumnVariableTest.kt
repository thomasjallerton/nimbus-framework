package com.nimbusframework.nimbuscore.clients.store.conditions.variable

import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe

class ColumnVariableTest : AnnotationSpec() {

    @Test
    fun getsColumnName() {
        val columnVariable = ColumnVariable("testColumn")
        columnVariable.getValue() shouldBe "testColumn"
    }

}