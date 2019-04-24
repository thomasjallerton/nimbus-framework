package com.nimbusframework.nimbuscore.clients.function

import com.nimbusframework.nimbuscore.clients.PermissionException

class EmptyBasicServerlessFunctionClient: BasicServerlessFunctionClient {
    private val clientName = "BasicServerlessFunctionClient"

    override fun invoke(handlerClass: Class<out Any>, functionName: String) {
        throw PermissionException(clientName)
    }

    override fun invoke(handlerClass: Class<out Any>, functionName: String, param: Any) {
        throw PermissionException(clientName)
    }

    override fun <T> invoke(handlerClass: Class<out Any>, functionName: String, responseType: Class<T>): T? {
        throw PermissionException(clientName)
    }

    override fun <T> invoke(handlerClass: Class<out Any>, functionName: String, param: Any, responseType: Class<T>): T? {
        throw PermissionException(clientName)
    }

    override fun invokeAsync(handlerClass: Class<out Any>, functionName: String) {
        throw PermissionException(clientName)
    }

    override fun invokeAsync(handlerClass: Class<out Any>, functionName: String, param: Any) {
        throw PermissionException(clientName)
    }
}