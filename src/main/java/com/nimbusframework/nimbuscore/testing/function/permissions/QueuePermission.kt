package com.nimbusframework.nimbuscore.testing.function.permissions

class QueuePermission(
        private val queueId: String
): Permission {
    override fun hasPermission(value: String): Boolean {
        return queueId == value
    }
}