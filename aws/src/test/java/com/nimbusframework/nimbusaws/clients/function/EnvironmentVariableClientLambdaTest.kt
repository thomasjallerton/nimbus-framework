package com.nimbusframework.nimbusaws.clients.function

import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.extensions.system.withEnvironment
import io.kotest.matchers.shouldBe

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