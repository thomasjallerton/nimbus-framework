package com.nimbusframework.nimbuslocal.deployment.function.permissions

interface Permission {

    fun hasPermission(value: String): Boolean
}