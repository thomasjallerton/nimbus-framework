package com.nimbusframework.nimbuscore.testing.function.information

import com.nimbusframework.nimbuscore.testing.function.FunctionType

data class NotificationFunctionInformation(
        val notificationTopic: String
): FunctionInformation(FunctionType.NOTIFICATION)