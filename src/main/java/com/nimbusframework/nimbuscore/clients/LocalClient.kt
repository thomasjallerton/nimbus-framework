package com.nimbusframework.nimbuscore.clients

import com.nimbusframework.nimbuscore.testing.LocalNimbusDeployment
import com.nimbusframework.nimbuscore.testing.function.FunctionEnvironment
import com.nimbusframework.nimbuscore.testing.function.FunctionIdentifier
import com.nimbusframework.nimbuscore.testing.function.PermissionType

abstract class LocalClient {

    protected var functionEnvironment: FunctionEnvironment? = null
    protected val localNimbusDeployment = LocalNimbusDeployment.getInstance()

    protected abstract val clientName: String
    protected abstract fun canUse(): Boolean

    private var checkedCanUse: Boolean = false


    protected fun getCallingServerlessMethod(): FunctionEnvironment? {
        val functionEnvironments = localNimbusDeployment.getFunctionEnvironments()
        val stElements = Thread.currentThread().stackTrace
        for (i in 1 until stElements.size) {
            val ste = stElements[i]
            val identifier = FunctionIdentifier(ste.className, ste.methodName)
            if (functionEnvironments.containsKey(identifier)) {
                return functionEnvironments[identifier]
            }
        }
        return null
    }

    protected fun checkPermissions(permissionsType: PermissionType, value: String): Boolean {
        functionEnvironment = getCallingServerlessMethod()
        if (functionEnvironment != null) {
            val permissions = functionEnvironment!!.getPermissions(permissionsType)
            for (permission in permissions) {
                if (permission.hasPermission(value)) {
                    return true
                }
            }
            return false
        } else {
            return true
        }
    }

    protected fun checkClientUse() {
        if (!checkedCanUse) {
            if (!canUse()) {
                throw PermissionException(clientName)
            }
            checkedCanUse = true
        }
    }


}