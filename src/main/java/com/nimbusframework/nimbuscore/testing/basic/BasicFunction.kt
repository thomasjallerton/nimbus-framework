package com.nimbusframework.nimbuscore.testing.basic

import com.nimbusframework.nimbuscore.testing.ServerlessMethod
import com.nimbusframework.nimbuscore.wrappers.basic.models.BasicEvent
import java.lang.reflect.Method

class BasicFunction(private val method: Method, private val invokeOn: Any) : ServerlessMethod(method, BasicEvent::class.java) {

    fun invoke() {
        invokeMethod(null, Unit.javaClass)
    }

    fun invoke(param: Any) {
        invokeMethod(param, Unit.javaClass)
    }

    fun <T> invoke(responseType: Class<T>): T? {
        return invokeMethod(null, responseType)
    }

    fun <T> invoke(param: Any, responseType: Class<T>): T? {
        return invokeMethod(param, responseType)
    }

    fun invokeAsync() {
        invokeMethod(null, Unit.javaClass)
    }

    fun invokeAsync(param: Any) {
        invokeMethod(param, Unit.javaClass)
    }

    private fun <T> invokeMethod(param: Any?, responseType: Class<T>): T? {
        timesInvoked++
        mostRecentInvokeArgument = param

        val params = method.parameters
        val eventIndex = eventIndex()

        val event = BasicEvent()

        val returnValue =when {
            params.isEmpty() -> method.invoke(invokeOn)
            params.size == 1 && eventIndex == 0 -> method.invoke(invokeOn, event)
            params.size == 1 -> method.invoke(invokeOn, param)
            else -> { //Params.size == 2
                if (eventIndex == 0) {
                    method.invoke(invokeOn, event, param)
                } else {
                    method.invoke(invokeOn, param, event)
                }
            }
        } as T
        mostRecentValueReturned = returnValue
        return returnValue
    }

}