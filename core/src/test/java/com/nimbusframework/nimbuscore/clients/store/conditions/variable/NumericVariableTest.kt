package com.nimbusframework.nimbuscore.clients.store.conditions.variable

import io.kotlintest.specs.AnnotationSpec
import io.kotlintest.shouldBe

class NumericVariableTest : AnnotationSpec() {

    @Test
    fun getsNumberVariable() {
        val numberVariable = NumericVariable(10)
        numberVariable.getValue() shouldBe 10
    }

}