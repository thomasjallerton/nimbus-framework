package com.nimbusframework.nimbusawslocal.aws.apigateway

import com.amazonaws.services.lambda.runtime.events.APIGatewayCustomAuthorizerEvent
import com.nimbusframework.nimbusaws.wrappers.customRuntimeEntry.CustomContext
import exampleresources.authorizers.ExampleAuthorizer
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.util.*

internal class AuthorizationFunctionTest: StringSpec({

    val ARN_PREFIX = "arn:aws:execute-api:"
    val LOCAL_REGION = "local-here-1"
    val ACCOUNT_ID = "000000000"
    val API_ID = "111111111"
    val LOCAL_STAGE = "local"

    "Can invoke custom function" {
        val method = ExampleAuthorizer::class.java.methods[0]
        val underTest = AuthorizationFunction(method, ExampleAuthorizer())

        val arn = "$ARN_PREFIX${LOCAL_REGION}:$ACCOUNT_ID:$API_ID/$LOCAL_STAGE/GET/hello/world"
        val event = APIGatewayCustomAuthorizerEvent.builder()
            .withMethodArn(arn)
            .withAuthorizationToken("TOKEN")
            .withHttpMethod("GET")
            .withPath("/hello/world")
            .build()

        val newPolicy = underTest.invokeMethod(event, CustomContext(UUID.randomUUID().toString()))

        val statements = newPolicy.policyDocument["Statement"]!! as Array<Map<String, Any>>
        val statement = statements[0]

        (statement["Resource"] as Array<String>)[0] shouldBe "$ARN_PREFIX${LOCAL_REGION}:$ACCOUNT_ID:$API_ID/$LOCAL_STAGE/*"
        statement["Effect"] shouldBe "Allow"

        newPolicy.context["key"] shouldBe "value"

    }

})
