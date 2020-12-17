package com.nimbusframework.nimbuscore.clients.store.conditions.variable

import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe

class BooleanVariableTest : AnnotationSpec() {

    @Test
    fun getsCorrectValueTrue() {
        val trueValue = BooleanVariable(true)
        trueValue.getValue() shouldBe true
    }

    @Test
    fun getsCorrectValueFalse() {
        val trueValue = BooleanVariable(false)
        trueValue.getValue() shouldBe false
    }

}