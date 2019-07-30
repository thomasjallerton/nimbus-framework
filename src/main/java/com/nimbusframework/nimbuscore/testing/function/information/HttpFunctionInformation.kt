package com.nimbusframework.nimbuscore.testing.function.information

import com.nimbusframework.nimbuscore.annotation.annotations.function.HttpMethod
import com.nimbusframework.nimbuscore.testing.function.FunctionType

data class HttpFunctionInformation(
        val httpMethod: HttpMethod,
        val httpPath: String
): FunctionInformation(FunctionType.HTTP)