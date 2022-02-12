package com.nimbusframework.nimbuslocal.deployment.function.information

import com.nimbusframework.nimbuslocal.deployment.function.FunctionType

data class NotificationFunctionInformation(
        val notificationTopic: String
): FunctionInformation(FunctionType.NOTIFICATION)