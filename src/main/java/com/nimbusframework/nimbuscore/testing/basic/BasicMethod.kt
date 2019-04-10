package com.nimbusframework.nimbuscore.testing.basic

import com.nimbusframework.nimbuscore.testing.ServerlessMethod
import java.lang.reflect.Method

class BasicMethod(private val method: Method, private val invokeOn: Any) : ServerlessMethod(method, null) {

    fun <T> invoke(param: Any?, responseType: Class<T>): T? {
        timesInvoked++
        mostRecentInvokeArgument = param
        val returnValue = if (param != null) {
            method.invoke(invokeOn, param) as T
        } else {
            method.invoke(invokeOn) as T
        }
        mostRecentValueReturned = returnValue
        return returnValue
    }

}