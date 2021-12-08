package com.nimbusframework.nimbuslocal.deployment.function.information

import com.nimbusframework.nimbuslocal.deployment.function.FunctionType

data class BasicFunctionInformation(
        val cronRule: String
) : FunctionInformation(FunctionType.BASIC)