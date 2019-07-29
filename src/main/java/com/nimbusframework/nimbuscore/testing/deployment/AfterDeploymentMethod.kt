package com.nimbusframework.nimbuscore.testing.deployment

import com.nimbusframework.nimbuscore.testing.ServerlessMethod
import com.nimbusframework.nimbuscore.testing.function.FunctionType
import com.nimbusframework.nimbuscore.wrappers.basic.models.BasicEvent
import java.lang.reflect.Method

class AfterDeploymentMethod(
        private val method: Method,
        private val invokeOn: Any
): ServerlessMethod(method, BasicEvent::class.java, FunctionType.BASIC) {


    fun invoke() {
        val result = if (method.parameters.isEmpty()) {
            method.invoke(invokeOn)
        } else {
            method.invoke(invokeOn, BasicEvent())
        }

        if (result != null) {
            println("After deployment ${method.name} returned $result")
        }
    }
}