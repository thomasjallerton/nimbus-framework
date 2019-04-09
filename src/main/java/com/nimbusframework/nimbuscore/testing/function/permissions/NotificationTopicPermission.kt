package com.nimbusframework.nimbuscore.testing.function.permissions

class NotificationTopicPermission(
        private val topicName: String
): Permission {
    override fun hasPermission(value: String): Boolean {
        return topicName == value
    }
}