package com.nimbusframework.nimbuscore.clients.function

import com.nimbusframework.nimbuscore.clients.LocalClient
import com.nimbusframework.nimbuscore.clients.PermissionException
import com.nimbusframework.nimbuscore.clients.file.FileStorageClient
import com.nimbusframework.nimbuscore.testing.function.PermissionType

internal class BasicServerlessFunctionClientLocal: BasicServerlessFunctionClient, LocalClient() {

    override fun canUse(): Boolean {
        return checkPermissions(PermissionType.BASIC_FUNCTION, "")
    }

    override val clientName: String = BasicServerlessFunctionClient::class.java.simpleName

    override fun invoke(handlerClass: Class<out Any>, functionName: String) {
        checkClientUse()
        val method = localNimbusDeployment.getBasicMethod(handlerClass, functionName)
        method.invoke("", Unit.javaClass)
    }

    override fun invoke(handlerClass: Class<out Any>, functionName: String, param: Any) {
        checkClientUse()
        val method = localNimbusDeployment.getBasicMethod(handlerClass, functionName)
        method.invoke(param, Unit.javaClass)
    }

    override fun <T> invoke(handlerClass: Class<out Any>, functionName: String, param: Any, responseType: Class<T>): T? {
        checkClientUse()
        val method = localNimbusDeployment.getBasicMethod(handlerClass, functionName)
        return method.invoke(param, responseType)
    }

    override fun invokeAsync(handlerClass: Class<out Any>, functionName: String) {
        checkClientUse()
        val method = localNimbusDeployment.getBasicMethod(handlerClass, functionName)
        method.invoke("", Unit.javaClass)
    }

    override fun invokeAsync(handlerClass: Class<out Any>, functionName: String, param: Any) {
        checkClientUse()
        val method = localNimbusDeployment.getBasicMethod(handlerClass, functionName)
        method.invoke(param, Unit.javaClass)
    }
}