package com.nimbusframework.nimbusawslocal.aws.secretsmanager

import com.nimbusframework.nimbusaws.clients.AwsClientBuilder
import com.nimbusframework.nimbusawslocal.aws.AwsSpecificLocalDeployment
import com.nimbusframework.nimbuslocal.LocalNimbusDeployment
import exampleresources.UserPool
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

internal class LocalSecretManagerClientTest: StringSpec({

    "can get secret" {
        // given
        LocalNimbusDeployment.getNewInstance { it
            .withSpecificLocalDeployment(AwsSpecificLocalDeployment.newInstance())
            .withClasses(UserPool::class.java)
        }

        val localSecrets = AwsSpecificLocalDeployment.currentInstance().getLocalSecrets()
        localSecrets.addSecret("secret_name", "secret_arn", "secret_value")

        // when
        val underTest = AwsClientBuilder.getSecretManagerClient()
        underTest.getSecret("secret_name") shouldBe "secret_value"
    }

    "throws error if secret doesn't exist" {
        // given
        LocalNimbusDeployment.getNewInstance { it
            .withSpecificLocalDeployment(AwsSpecificLocalDeployment.newInstance())
            .withClasses(UserPool::class.java)
        }

        val localSecrets = AwsSpecificLocalDeployment.currentInstance().getLocalSecrets()
        localSecrets.addSecret("secret_haha", "secret_arn", "secret_value")

        // when
        val underTest = AwsClientBuilder.getSecretManagerClient()
        shouldThrowAny { underTest.getSecret("secret_name") }
    }

})
