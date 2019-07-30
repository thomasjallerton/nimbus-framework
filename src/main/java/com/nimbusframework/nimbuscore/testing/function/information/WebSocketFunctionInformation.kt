package com.nimbusframework.nimbuscore.testing.function.information

import com.nimbusframework.nimbuscore.testing.function.FunctionType

data class WebSocketFunctionInformation(
        val topic: String
): FunctionInformation(FunctionType.WEBSOCKET)