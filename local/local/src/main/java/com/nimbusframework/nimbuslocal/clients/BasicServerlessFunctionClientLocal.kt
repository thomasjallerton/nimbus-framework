package com.nimbusframework.nimbuslocal.clients

import com.nimbusframework.nimbuscore.clients.function.BasicServerlessFunctionClient
import com.nimbusframework.nimbuscore.permissions.PermissionType

internal class BasicServerlessFunctionClientLocal(
        private val handlerClass: Class<out Any>,
        private val functionName: String
): BasicServerlessFunctionClient, LocalClient(PermissionType.BASIC_FUNCTION) {


    override fun canUse(permissionType: PermissionType): Boolean {
        return checkPermissions(permissionType, handlerClass.simpleName + functionName)
    }

    override val clientName: String = BasicServerlessFunctionClient::class.java.simpleName

    override fun invoke() {
        checkClientUse()
        val method = localNimbusDeployment.getBasicFunction(handlerClass, functionName)
        method.invoke()
    }

    override fun invoke(param: Any) {
        checkClientUse()
        val method = localNimbusDeployment.getBasicFunction(handlerClass, functionName)
        method.invoke(param, Unit.javaClass)
    }

    override fun <T> invoke(responseType: Class<T>): T? {
        checkClientUse()
        val method = localNimbusDeployment.getBasicFunction(handlerClass, functionName)
        return method.invoke(responseType)
    }

    override fun <T> invoke(param: Any, responseType: Class<T>): T? {
        checkClientUse()
        val method = localNimbusDeployment.getBasicFunction(handlerClass, functionName)
        return method.invoke(param, responseType)
    }

    override fun invokeAsync() {
        checkClientUse()
        val method = localNimbusDeployment.getBasicFunction(handlerClass, functionName)
        method.invokeAsync()
    }

    override fun invokeAsync(param: Any) {
        checkClientUse()
        val method = localNimbusDeployment.getBasicFunction(handlerClass, functionName)
        method.invokeAsync(param)
    }
}
