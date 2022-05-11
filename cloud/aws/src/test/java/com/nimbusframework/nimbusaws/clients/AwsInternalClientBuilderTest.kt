package com.nimbusframework.nimbusaws.clients

import com.nimbusframework.nimbusaws.examples.cognito.DefaultStagesCognito
import com.nimbusframework.nimbusaws.examples.cognito.MultipleStagesCognito
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.StringSpec
import software.amazon.awssdk.regions.Region

internal class AwsInternalClientBuilderTest: StringSpec({

    val underTest = AwsInternalClientBuilder.setRegion(Region.EU_WEST_1)

    "Can get cognito client when using default stages" {
        underTest.getCognitoClient(DefaultStagesCognito::class.java, "stage")
    }

    "Can get cognito client when using multiple stages" {
        underTest.getCognitoClient(MultipleStagesCognito::class.java, "stage1")
    }

    "throws error if using multiple stages stage doesn't exist" {
        shouldThrowAny { underTest.getCognitoClient(MultipleStagesCognito::class.java, "stage") }
    }

})
