package com.nimbusframework.nimbuscore.clients.store.conditions.variable

import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe

class NumericVariableTest : AnnotationSpec() {

    @Test
    fun getsNumberVariable() {
        val numberVariable = NumericVariable(10)
        numberVariable.getValue() shouldBe 10
    }

}