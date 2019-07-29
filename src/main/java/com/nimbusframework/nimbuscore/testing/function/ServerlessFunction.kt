package com.nimbusframework.nimbuscore.testing.function

import com.nimbusframework.nimbuscore.testing.ServerlessMethod
import com.nimbusframework.nimbuscore.testing.function.information.FunctionInformation

data class ServerlessFunction(
        val serverlessMethod: ServerlessMethod,
        val functionInformation: FunctionInformation
)