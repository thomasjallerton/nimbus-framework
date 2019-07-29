package com.nimbusframework.nimbuscore.testing.function.information

import com.nimbusframework.nimbuscore.annotation.annotations.function.HttpMethod

data class HttpFunctionInformation(
        val httpMethod: HttpMethod,
        val httpPath: String
): FunctionInformation()