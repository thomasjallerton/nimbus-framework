package com.nimbusframework.nimbuslocal

import com.nimbusframework.nimbuslocal.deployment.function.FunctionType
import java.lang.reflect.Method

abstract class ServerlessMethod(
        private val method: Method,
        private val eventType: Class<out Any>?,
        val type: FunctionType
) {
    var timesInvoked: Int = 0
        protected set
    var mostRecentInvokeArgument: Any? = null
        protected set
    var mostRecentValueReturned: Any? = null
        protected set

    protected fun eventIndex(): Int {
        for ((index, param) in method.parameterTypes.withIndex()) {
            if (param == eventType) {
                return index
            }
        }
        return -1
    }

    protected fun inputIndex(): Int {
        for ((index, param) in method.parameterTypes.withIndex()) {
            if (param != eventType) {
                return index
            }
        }
        return -1
    }
}
