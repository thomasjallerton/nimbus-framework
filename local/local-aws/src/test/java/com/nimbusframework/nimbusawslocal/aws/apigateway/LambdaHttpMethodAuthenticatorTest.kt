package com.nimbusframework.nimbusawslocal.aws.apigateway

import com.nimbusframework.nimbuscore.annotations.http.HttpMethod
import com.nimbusframework.nimbuslocal.deployment.function.FunctionIdentifier
import com.nimbusframework.nimbuslocal.deployment.function.FunctionType
import com.nimbusframework.nimbuslocal.deployment.function.ServerlessFunction
import com.nimbusframework.nimbuslocal.deployment.function.information.FunctionInformation
import com.nimbusframework.nimbuslocal.deployment.http.HttpRequest
import com.nimbusframework.nimbuslocal.deployment.services.LocalResourceHolder
import exampleresources.authorizers.ExampleAuthorizer
import exampleresources.authorizers.ExampleAuthorizerAllStars
import exampleresources.authorizers.ExampleAuthorizerDenySpecific
import exampleresources.authorizers.ExampleAuthorizerPathStar
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.shouldBe

internal class LambdaHttpMethodAuthenticatorTest: StringSpec({

    "Can validate http call when allowed" {
        val resourceHolder = LocalResourceHolder()
        val identifier = FunctionIdentifier(ExampleAuthorizer::class.java, "handleRequest")
        val method = ExampleAuthorizer::class.java.methods[0]
        resourceHolder.functions[identifier] = ServerlessFunction(AuthorizationFunction(method, ExampleAuthorizer()), FunctionInformation(FunctionType.AUTHORIZATION))

        val underTest = LambdaHttpMethodAuthenticator(
            identifier,
            "Authorization",
            10,
            resourceHolder
        )

        // when
        val result = underTest.allow(HttpRequest("/hello/world", HttpMethod.GET, headers = mapOf(Pair("Authorization", listOf("token")))))
        result.authenticated shouldBe true
        result.context["key"] shouldBe "value"
    }

    "Uses cache" {
        val resourceHolder = LocalResourceHolder()
        val identifier = FunctionIdentifier(ExampleAuthorizer::class.java, "handleRequest")
        val method = ExampleAuthorizer::class.java.methods[0]
        resourceHolder.functions[identifier] = ServerlessFunction(AuthorizationFunction(method, ExampleAuthorizer()), FunctionInformation(FunctionType.AUTHORIZATION))

        val underTest = LambdaHttpMethodAuthenticator(
            identifier,
            "Authorization",
            10,
            resourceHolder
        )

        // when
        val result = underTest.allow(HttpRequest("/hello/world", HttpMethod.GET, headers = mapOf(Pair("Authorization", listOf("token")))))
        underTest.allow(HttpRequest("/hello/world", HttpMethod.GET, headers = mapOf(Pair("Authorization", listOf("token")))))

        resourceHolder.functions[identifier]!!.serverlessMethod.timesInvoked shouldBe 1
    }

    "Does not use cache if 0" {
        val resourceHolder = LocalResourceHolder()
        val identifier = FunctionIdentifier(ExampleAuthorizer::class.java, "handleRequest")
        val method = ExampleAuthorizer::class.java.methods[0]
        resourceHolder.functions[identifier] = ServerlessFunction(AuthorizationFunction(method, ExampleAuthorizer()), FunctionInformation(FunctionType.AUTHORIZATION))

        val underTest = LambdaHttpMethodAuthenticator(
            identifier,
            "Authorization",
            0,
            resourceHolder
        )

        // when
        val result = underTest.allow(HttpRequest("/hello/world", HttpMethod.GET, headers = mapOf(Pair("Authorization", listOf("token")))))
        underTest.allow(HttpRequest("/hello/world", HttpMethod.GET, headers = mapOf(Pair("Authorization", listOf("token")))))

        resourceHolder.functions[identifier]!!.serverlessMethod.timesInvoked shouldBe 2
    }

    "Defaults to false if header not present" {
        val resourceHolder = LocalResourceHolder()
        val identifier = FunctionIdentifier(ExampleAuthorizer::class.java, "handleRequest")
        val method = ExampleAuthorizer::class.java.methods[0]
        resourceHolder.functions[identifier] = ServerlessFunction(AuthorizationFunction(method, ExampleAuthorizer()), FunctionInformation(FunctionType.AUTHORIZATION))

        val underTest = LambdaHttpMethodAuthenticator(
            identifier,
            "Authorization",
            10,
            resourceHolder
        )

        // when
        val result = underTest.allow(HttpRequest("/hello/world", HttpMethod.GET))
        result.authenticated shouldBe false
        result.context shouldHaveSize 0
    }

    "Allows if all stars" {
        val resourceHolder = LocalResourceHolder()
        val identifier = FunctionIdentifier(ExampleAuthorizerAllStars::class.java, "handleRequest")
        val method = ExampleAuthorizerAllStars::class.java.methods[0]
        resourceHolder.functions[identifier] = ServerlessFunction(AuthorizationFunction(method, ExampleAuthorizerAllStars()), FunctionInformation(FunctionType.AUTHORIZATION))

        val underTest = LambdaHttpMethodAuthenticator(
            identifier,
            "Authorization",
            10,
            resourceHolder
        )

        // when
        val result = underTest.allow(HttpRequest("/hello/world", HttpMethod.GET, headers = mapOf(Pair("Authorization", listOf("token")))))
        result.authenticated shouldBe true
    }

    "Allows path ends in star" {
        val resourceHolder = LocalResourceHolder()
        val identifier = FunctionIdentifier(ExampleAuthorizerPathStar::class.java, "handleRequest")
        val method = ExampleAuthorizerPathStar::class.java.methods[0]
        resourceHolder.functions[identifier] = ServerlessFunction(AuthorizationFunction(method, ExampleAuthorizerPathStar()), FunctionInformation(FunctionType.AUTHORIZATION))

        val underTest = LambdaHttpMethodAuthenticator(
            identifier,
            "Authorization",
            10,
            resourceHolder
        )

        // when
        val result = underTest.allow(HttpRequest("/hello/world", HttpMethod.GET, headers = mapOf(Pair("Authorization", listOf("token")))))
        result.authenticated shouldBe true
    }

    "Can deny" {
        val resourceHolder = LocalResourceHolder()
        val identifier = FunctionIdentifier(ExampleAuthorizerDenySpecific::class.java, "handleRequest")
        val method = ExampleAuthorizerDenySpecific::class.java.methods[0]
        resourceHolder.functions[identifier] = ServerlessFunction(AuthorizationFunction(method, ExampleAuthorizerDenySpecific()), FunctionInformation(FunctionType.AUTHORIZATION))

        val underTest = LambdaHttpMethodAuthenticator(
            identifier,
            "Authorization",
            10,
            resourceHolder
        )

        // when
        val result = underTest.allow(HttpRequest("/hello/world", HttpMethod.GET, headers = mapOf(Pair("Authorization", listOf("token")))))
        result.authenticated shouldBe false
        val result2 = underTest.allow(HttpRequest("/hello/other", HttpMethod.GET, headers = mapOf(Pair("Authorization", listOf("token")))))
        result2.authenticated shouldBe true
    }

    "Can validate http call when allowed - header is sent lowercase" {
        val resourceHolder = LocalResourceHolder()
        val identifier = FunctionIdentifier(ExampleAuthorizer::class.java, "handleRequest")
        val method = ExampleAuthorizer::class.java.methods[0]
        resourceHolder.functions[identifier] = ServerlessFunction(AuthorizationFunction(method, ExampleAuthorizer()), FunctionInformation(FunctionType.AUTHORIZATION))

        val underTest = LambdaHttpMethodAuthenticator(
            identifier,
            "Authorization",
            10,
            resourceHolder
        )

        // when
        val result = underTest.allow(HttpRequest("/hello/world", HttpMethod.GET, headers = mapOf(Pair("authorization", listOf("token")))))
        result.authenticated shouldBe true
        result.context["key"] shouldBe "value"
    }


})
