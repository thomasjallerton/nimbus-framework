package com.nimbusframework.nimbuslocal.deployment.function.permissions

class BasicFunctionPermission(
        private val targetClazz: Class<out Any>,
        private val method: String
): Permission {

    override fun hasPermission(value: String): Boolean {
        return value == (targetClazz.simpleName + method)
    }
}