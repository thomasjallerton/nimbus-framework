package com.nimbusframework.nimbuscore.clients.store.conditions.variable

import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe

class StringVariableTest : AnnotationSpec() {

    @Test
    fun getsStringVariable() {
        val stringVariable = StringVariable("stringVal")
        stringVariable.getValue() shouldBe "stringVal"
    }

}