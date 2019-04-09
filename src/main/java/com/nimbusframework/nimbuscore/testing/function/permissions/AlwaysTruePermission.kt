package com.nimbusframework.nimbuscore.testing.function.permissions

class AlwaysTruePermission: Permission {
    override fun hasPermission(value: String): Boolean {
        return true
    }
}