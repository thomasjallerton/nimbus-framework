package com.nimbusframework.nimbuslocal.deployment.function.permissions

class StorePermission(
        private val dataModel: String
): Permission {
    override fun hasPermission(value: String): Boolean {
        return value == dataModel
    }
}