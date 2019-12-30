package com.nimbusframework.nimbusaws.clients.function

import io.kotlintest.extensions.system.withEnvironment
import io.kotlintest.shouldBe
import io.kotlintest.specs.AnnotationSpec

class EnvironmentVariableClientLambdaTest: AnnotationSpec() {

    private val underTest = EnvironmentVariableClientLambda()

    @Test
    fun canGetEnvironmentVariable() {

        withEnvironment(Pair("Key", "Value")) {
            underTest.get("Key") shouldBe "Value"
        }

    }

    @Test
    fun canCheckEnvionmentVariablesContainsKey() {

        withEnvironment(Pair("Key", "Value")) {
            underTest.containsKey("Key") shouldBe true
        }
    }

}