package com.nimbusframework.nimbuslocal.deployment.webserver.webconsole.models

import com.nimbusframework.nimbuslocal.deployment.notification.SubscriberInformation

data class NotificationInformation(
        val topicName: String,
        val subscribers: Int,
        val generalSubscribers: List<SubscriberInformation>,
        val functionSubscribers: List<FunctionSubscriberInformation>,
        val totalNotifications: Int
)