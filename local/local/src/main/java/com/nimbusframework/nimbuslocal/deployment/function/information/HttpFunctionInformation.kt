package com.nimbusframework.nimbuslocal.deployment.function.information

import com.nimbusframework.nimbuscore.annotations.http.HttpMethod
import com.nimbusframework.nimbuslocal.deployment.function.FunctionType

data class HttpFunctionInformation(
    val httpMethod: HttpMethod,
    val httpPath: String
): FunctionInformation(FunctionType.HTTP)
