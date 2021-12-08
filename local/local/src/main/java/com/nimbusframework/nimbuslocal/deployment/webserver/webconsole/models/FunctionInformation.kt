package com.nimbusframework.nimbuslocal.deployment.webserver.webconsole.models

import com.nimbusframework.nimbuslocal.deployment.function.FunctionType

data class FunctionInformation(
        val className: String,
        val qualifiedClassName: String,
        val methodName: String,
        val timesInvoked: Int,
        val type: FunctionType
)