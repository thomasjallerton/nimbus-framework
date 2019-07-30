package com.nimbusframework.nimbuscore.testing.function.information

import com.nimbusframework.nimbuscore.testing.function.FunctionType

data class QueueFunctionInformation(
        val queueName: String,
        val batchSize: Int
): FunctionInformation(FunctionType.QUEUE)