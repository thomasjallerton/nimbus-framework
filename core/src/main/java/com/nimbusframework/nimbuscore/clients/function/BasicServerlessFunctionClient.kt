package com.nimbusframework.nimbuscore.clients.function

interface BasicServerlessFunctionClient {
    fun invoke()
    fun invoke(param: Any)
    fun <T> invoke(responseType: Class<T>): T?
    fun <T> invoke(param: Any, responseType: Class<T>): T?

    fun invokeAsync()
    fun invokeAsync(param: Any)
}