package com.nimbusframework.nimbuscore.testing.webserver.webconsole.models

import com.nimbusframework.nimbuscore.testing.function.FunctionType

data class FunctionInformation(
        val className: String,
        val qualifiedClassName: String,
        val methodName: String,
        val timesInvoked: Int,
        val type: FunctionType
)