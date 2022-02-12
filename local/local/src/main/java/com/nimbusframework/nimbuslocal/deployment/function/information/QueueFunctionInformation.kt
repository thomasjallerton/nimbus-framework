package com.nimbusframework.nimbuslocal.deployment.function.information

import com.nimbusframework.nimbuslocal.deployment.function.FunctionType

data class QueueFunctionInformation(
        val queueName: String,
        val batchSize: Int
): FunctionInformation(FunctionType.QUEUE)