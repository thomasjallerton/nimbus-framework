package com.nimbusframework.nimbuscore.testing.webserver.webconsole.models

import com.nimbusframework.nimbuscore.testing.notification.SubscriberInformation

data class NotificationInformation(
        val topicName: String,
        val subscribers: Int,
        val generalSubscribers: List<SubscriberInformation>,
        val functionSubscribers: List<FunctionSubscriberInformation>,
        val totalNotifications: Int
)