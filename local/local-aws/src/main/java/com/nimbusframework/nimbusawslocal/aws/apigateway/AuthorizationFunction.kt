package com.nimbusframework.nimbusawslocal.aws.apigateway

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayCustomAuthorizerEvent
import com.amazonaws.services.lambda.runtime.events.IamPolicyResponse
import com.nimbusframework.nimbuslocal.ServerlessMethod
import com.nimbusframework.nimbuslocal.deployment.function.FunctionType
import com.nimbusframework.nimbuscore.eventabstractions.BasicEvent
import java.lang.reflect.Method

class AuthorizationFunction(private val method: Method, private val invokeOn: Any)
    : ServerlessMethod(method, Unit::class.java, FunctionType.AUTHORIZATION) {

    fun invokeMethod(input: APIGatewayCustomAuthorizerEvent?, context: Context?): IamPolicyResponse {
        timesInvoked++
        mostRecentInvokeArgument = Pair(input, context)

        return method.invoke(invokeOn, input, context) as IamPolicyResponse
    }

}
