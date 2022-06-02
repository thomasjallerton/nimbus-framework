package com.nimbusframework.nimbusawslocal.aws.apigateway

import com.nimbusframework.nimbusaws.clients.cognito.CognitoUser
import com.nimbusframework.nimbusawslocal.aws.LocalAwsResourceHolder
import com.nimbusframework.nimbusawslocal.aws.cognito.LocalCognito
import com.nimbusframework.nimbuscore.annotations.function.HttpMethod
import com.nimbusframework.nimbuslocal.deployment.http.HttpRequest
import exampleresources.UserPool
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

internal class CognitoHttpMethodAuthenticatorTest: StringSpec({

    "Can authenticate a cognito user & cache" {
        val resourceHolder = LocalAwsResourceHolder()
        val localCognito = LocalCognito("ard")
        resourceHolder.cognitoUserPools[UserPool::class.java] = localCognito

        localCognito.addUser("token", CognitoUser("hello", mapOf()))

        val underTest = CognitoHttpMethodAuthenticator(UserPool::class.java, "Authorization", 10, resourceHolder)

        val result = underTest.allow(HttpRequest("/hello/world", HttpMethod.GET, headers = mapOf(Pair("Authorization", listOf("token")))))
        result.authenticated shouldBe true

        localCognito.removeUser("hello")
        val cachedResult = underTest.allow(HttpRequest("/hello/world", HttpMethod.GET, headers = mapOf(Pair("Authorization", listOf("token")))))
        cachedResult.authenticated shouldBe true
    }

    "Will fail if wrong header supplied" {
        val resourceHolder = LocalAwsResourceHolder()
        val localCognito = LocalCognito("ard")
        resourceHolder.cognitoUserPools[UserPool::class.java] = localCognito

        localCognito.addUser("token", CognitoUser("hello", mapOf()))

        val underTest = CognitoHttpMethodAuthenticator(UserPool::class.java, "Test", 10, resourceHolder)

        val result = underTest.allow(HttpRequest("/hello/world", HttpMethod.GET, headers = mapOf(Pair("Authorization", listOf("token")))))
        result.authenticated shouldBe false
    }

    "Will reject unauthenticated user" {
        val resourceHolder = LocalAwsResourceHolder()
        val localCognito = LocalCognito("ard")
        resourceHolder.cognitoUserPools[UserPool::class.java] = localCognito

        val underTest = CognitoHttpMethodAuthenticator(UserPool::class.java, "Authorization", 10, resourceHolder)

        val result = underTest.allow(HttpRequest("/hello/world", HttpMethod.GET, headers = mapOf(Pair("Authorization", listOf("token")))))
        result.authenticated shouldBe false
    }

    "Will not cache if ttl is 0" {
        val resourceHolder = LocalAwsResourceHolder()
        val localCognito = LocalCognito("ard")
        resourceHolder.cognitoUserPools[UserPool::class.java] = localCognito

        localCognito.addUser("token", CognitoUser("hello", mapOf()))

        val underTest = CognitoHttpMethodAuthenticator(UserPool::class.java, "Authorization", 0, resourceHolder)

        val result = underTest.allow(HttpRequest("/hello/world", HttpMethod.GET, headers = mapOf(Pair("Authorization", listOf("token")))))
        result.authenticated shouldBe true

        localCognito.removeUser("hello")
        val cachedResult = underTest.allow(HttpRequest("/hello/world", HttpMethod.GET, headers = mapOf(Pair("Authorization", listOf("token")))))
        cachedResult.authenticated shouldBe false
    }

})
