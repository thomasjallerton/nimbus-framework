package com.nimbusframework.nimbuscore.testing.notification

import com.nimbusframework.nimbuscore.clients.notification.Protocol

data class SubscriberInformation(val protocol: Protocol, val endpoint: String)