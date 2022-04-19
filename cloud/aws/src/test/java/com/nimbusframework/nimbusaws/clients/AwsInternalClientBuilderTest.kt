package com.nimbusframework.nimbusaws.clients

import com.nimbusframework.nimbusaws.examples.cognito.DefaultStagesCognito
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.StringSpec

internal class AwsInternalClientBuilderTest: StringSpec({

    val underTest = AwsInternalClientBuilder

    "Can get cognito client when using default stages" {
        underTest.getCognitoClient(DefaultStagesCognito::class.java, "stage")
    }

    "Can get cognito client when using multiple stages" {
        underTest.getCognitoClient(DefaultStagesCognito::class.java, "stage1")
    }

    "throws error if using multiple stages stage doesn't exist" {
        shouldThrowAny { underTest.getCognitoClient(DefaultStagesCognito::class.java, "stage") }
    }

})
