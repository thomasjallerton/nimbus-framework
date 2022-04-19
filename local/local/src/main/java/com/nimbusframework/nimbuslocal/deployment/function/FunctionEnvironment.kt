package com.nimbusframework.nimbuslocal.deployment.function

import com.nimbusframework.nimbuscore.permissions.PermissionType
import com.nimbusframework.nimbuslocal.deployment.function.permissions.Permission

class FunctionEnvironment {

    private val environmentVariables: MutableMap<String, String> = mutableMapOf()
    private val permissions: MutableMap<String, MutableList<Permission>> = mutableMapOf()

    fun addPermission(permissionType: PermissionType, permission: Permission) {
        if (permissions.containsKey(permissionType.getKey())) {
            permissions[permissionType.getKey()]!!.add(permission)
        } else {
            permissions[permissionType.getKey()] = mutableListOf(permission)
        }
    }

    fun getPermissions(permissionType: PermissionType): List<Permission> {
        return if (permissions[permissionType.getKey()] != null) permissions[permissionType.getKey()]!! else listOf()
    }

    fun addEnvironmentVariable(key: String, value: String) {
        environmentVariables[key] = value
    }

    fun getEnvironmentVariables(): Map<String, String> {
        return environmentVariables
    }

}
