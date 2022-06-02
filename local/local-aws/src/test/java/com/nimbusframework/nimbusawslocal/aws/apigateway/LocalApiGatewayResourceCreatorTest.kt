package com.nimbusframework.nimbusawslocal.aws.apigateway

import com.nimbusframework.nimbusaws.clients.cognito.CognitoUser
import com.nimbusframework.nimbusawslocal.aws.AwsSpecificLocalDeployment
import com.nimbusframework.nimbuscore.annotations.function.HttpMethod
import com.nimbusframework.nimbuslocal.LocalNimbusDeployment
import com.nimbusframework.nimbuslocal.deployment.http.HttpRequest
import exampleresources.LambdaApiGatewayAuthorizer
import exampleresources.UserPool
import exampleresources.authorizers.CognitoApiGatewayAuthorizer
import exampleresources.authorizers.ExampleAuthorizerPathStar
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

internal class LocalApiGatewayResourceCreatorTest: StringSpec({

    "Can add a lambda api gateway authorizer" {
        val localDeployment = LocalNimbusDeployment.getNewInstance { it
            .withClasses(LambdaApiGatewayAuthorizer::class.java, ExampleAuthorizerPathStar::class.java)
            .withSpecificLocalDeployment(AwsSpecificLocalDeployment.newInstance())
        }

        localDeployment.getMethod(ExampleAuthorizerPathStar::class.java, "handleRequest").timesInvoked shouldBe 0

        val result = localDeployment.sendHttpRequest(HttpRequest("hello/world", HttpMethod.GET, headers = mapOf(Pair("Authorization", listOf("token")))))
        result shouldBe "helloworld"

        localDeployment.getMethod(ExampleAuthorizerPathStar::class.java, "handleRequest").timesInvoked shouldBe 1

        shouldThrowAny { localDeployment.sendHttpRequest(HttpRequest("hello/world", HttpMethod.PUT, headers = mapOf(Pair("Authorization", listOf("token"))))) }

        localDeployment.getMethod(ExampleAuthorizerPathStar::class.java, "handleRequest").timesInvoked shouldBe 1
    }

    "Can add a cognito api gateway authorizer" {
        val localDeployment = LocalNimbusDeployment.getNewInstance { it
            .withClasses(CognitoApiGatewayAuthorizer::class.java, UserPool::class.java)
            .withSpecificLocalDeployment(AwsSpecificLocalDeployment.newInstance())
        }

        AwsSpecificLocalDeployment.currentInstance().getUserPool(UserPool::class.java).addUser("token", CognitoUser("hello", mapOf()))

        val result = localDeployment.sendHttpRequest(HttpRequest("hello/world", HttpMethod.GET, headers = mapOf(Pair("Authorization", listOf("token")))))
        result shouldBe "helloworld"
    }

})
