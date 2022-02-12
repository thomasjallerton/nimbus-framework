package com.nimbusframework.nimbuslocal.deployment.notification

import com.nimbusframework.nimbuscore.clients.notification.Protocol

data class SubscriberInformation(val protocol: Protocol, val endpoint: String)