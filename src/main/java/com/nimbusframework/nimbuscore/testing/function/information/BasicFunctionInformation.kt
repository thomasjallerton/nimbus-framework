package com.nimbusframework.nimbuscore.testing.function.information

import com.nimbusframework.nimbuscore.testing.function.FunctionType

data class BasicFunctionInformation(
        val cronRule: String
) : FunctionInformation(FunctionType.BASIC)