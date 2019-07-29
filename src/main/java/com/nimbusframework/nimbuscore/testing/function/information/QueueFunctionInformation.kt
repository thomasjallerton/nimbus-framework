package com.nimbusframework.nimbuscore.testing.function.information

data class QueueFunctionInformation(
        val queueName: String,
        val batchSize: Int
): FunctionInformation()