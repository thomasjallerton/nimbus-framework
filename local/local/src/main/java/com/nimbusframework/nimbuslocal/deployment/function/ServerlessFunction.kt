package com.nimbusframework.nimbuslocal.deployment.function

import com.nimbusframework.nimbuslocal.ServerlessMethod
import com.nimbusframework.nimbuslocal.deployment.function.information.FunctionInformation

data class ServerlessFunction(
        val serverlessMethod: ServerlessMethod,
        val functionInformation: FunctionInformation
)