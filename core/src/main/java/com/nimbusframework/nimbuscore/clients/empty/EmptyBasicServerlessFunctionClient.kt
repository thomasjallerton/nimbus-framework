package com.nimbusframework.nimbuscore.clients.empty

import com.nimbusframework.nimbuscore.clients.function.BasicServerlessFunctionClient
import com.nimbusframework.nimbuscore.exceptions.PermissionException


class EmptyBasicServerlessFunctionClient: BasicServerlessFunctionClient {
    private val clientName = "BasicServerlessFunctionClient"

    override fun invoke() {
        throw PermissionException(clientName)
    }

    override fun invoke(param: Any) {
        throw PermissionException(clientName)
    }

    override fun <T> invoke(responseType: Class<T>): T? {
        throw PermissionException(clientName)
    }

    override fun <T> invoke(param: Any, responseType: Class<T>): T? {
        throw PermissionException(clientName)
    }

    override fun invokeAsync() {
        throw PermissionException(clientName)
    }

    override fun invokeAsync(param: Any) {
        throw PermissionException(clientName)
    }
}