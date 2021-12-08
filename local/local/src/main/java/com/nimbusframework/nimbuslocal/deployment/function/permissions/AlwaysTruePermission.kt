package com.nimbusframework.nimbuslocal.deployment.function.permissions

class AlwaysTruePermission: Permission {
    override fun hasPermission(value: String): Boolean {
        return true
    }
}