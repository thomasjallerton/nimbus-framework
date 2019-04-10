package com.nimbusframework.nimbuscore.clients.function

interface BasicServerlessFunctionClient {
    fun invoke(handlerClass: Class<out Any>, functionName: String)
    fun invoke(handlerClass: Class<out Any>, functionName: String, param: Any)
    fun <T> invoke(handlerClass: Class<out Any>, functionName: String, responseType: Class<T>): T?
    fun <T> invoke(handlerClass: Class<out Any>, functionName: String, param: Any, responseType: Class<T>): T?

    fun invokeAsync(handlerClass: Class<out Any>, functionName: String)
    fun invokeAsync(handlerClass: Class<out Any>, functionName: String, param: Any)
}