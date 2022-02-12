package com.nimbusframework.nimbuslocal.deployment.function.permissions

class QueuePermission(
        private val queueId: String
): Permission {
    override fun hasPermission(value: String): Boolean {
        return queueId == value
    }
}