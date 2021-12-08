package com.nimbusframework.nimbuslocal.deployment.function.information

import com.nimbusframework.nimbuslocal.deployment.function.FunctionType

data class WebSocketFunctionInformation(
        val topic: String
): FunctionInformation(FunctionType.WEBSOCKET)