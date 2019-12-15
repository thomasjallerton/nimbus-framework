package com.nimbusframework.nimbuscore.clients.store.conditions.variable

import io.kotlintest.specs.AnnotationSpec
import io.kotlintest.shouldBe

class StringVariableTest : AnnotationSpec() {

    @Test
    fun getsStringVariable() {
        val stringVariable = StringVariable("stringVal")
        stringVariable.getValue() shouldBe "stringVal"
    }

}