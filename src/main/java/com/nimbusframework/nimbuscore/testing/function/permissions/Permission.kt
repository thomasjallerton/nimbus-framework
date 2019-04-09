package com.nimbusframework.nimbuscore.testing.function.permissions

interface Permission {

    fun hasPermission(value: String): Boolean
}