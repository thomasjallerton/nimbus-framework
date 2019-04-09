package com.nimbusframework.nimbuscore.testing.function

import com.nimbusframework.nimbuscore.testing.function.permissions.Permission

class FunctionEnvironment {

    private val environmentVariables: MutableMap<String, String> = mutableMapOf()
    private val permissions: MutableMap<PermissionType, MutableList<Permission>> = mutableMapOf()

    fun addPermission(permissionType: PermissionType, permission: Permission) {
        if (permissions.containsKey(permissionType)) {
            permissions[permissionType]!!.add(permission)
        } else {
            permissions[permissionType] = mutableListOf(permission)
        }
    }

    fun getPermissions(permissionType: PermissionType): List<Permission> {
        return if (permissions[permissionType] != null) permissions[permissionType]!! else listOf()
    }

    fun addEnvironmentVariable(key: String, value: String) {
        environmentVariables[key] = value
    }

    fun getEnvironmentVariables(): Map<String, String> {
        return environmentVariables
    }

}