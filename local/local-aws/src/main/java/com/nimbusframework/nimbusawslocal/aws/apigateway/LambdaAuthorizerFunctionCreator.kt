package com.nimbusframework.nimbusawslocal.aws.apigateway

import com.nimbusframework.nimbusaws.interfaces.ApiGatewayLambdaAuthorizer
import com.nimbusframework.nimbusawslocal.aws.LocalAwsResourceHolder
import com.nimbusframework.nimbuslocal.deployment.basic.BasicFunction
import com.nimbusframework.nimbuslocal.deployment.function.FunctionIdentifier
import com.nimbusframework.nimbuslocal.deployment.function.FunctionType
import com.nimbusframework.nimbuslocal.deployment.function.ServerlessFunction
import com.nimbusframework.nimbuslocal.deployment.function.information.FunctionInformation
import com.nimbusframework.nimbuslocal.deployment.services.LocalResourceHolder
import com.nimbusframework.nimbuslocal.deployment.services.StageService
import com.nimbusframework.nimbuslocal.deployment.services.function.LocalFunctionHandler
import java.lang.reflect.Method

class LambdaAuthorizerFunctionCreator(
    private val localResourceHolder: LocalResourceHolder
) : LocalFunctionHandler(localResourceHolder) {

    override fun handleMethod(clazz: Class<out Any>, method: Method): Boolean {
        if (clazz.interfaces.contains(ApiGatewayLambdaAuthorizer::class.java) && method.name == "handleRequest") {
            val functionIdentifier = FunctionIdentifier(clazz.canonicalName, "handleRequest")

            val invokeOn = getFunctionClassInstance(clazz)
            localResourceHolder.functions[functionIdentifier] = ServerlessFunction(AuthorizationFunction(method, invokeOn), FunctionInformation(FunctionType.AUTHORIZATION))
            return true
        }
        return false
    }

}
