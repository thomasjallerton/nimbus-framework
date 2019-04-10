package com.nimbusframework.nimbuscore.testing.deployment

import java.lang.reflect.Method

class AfterDeploymentMethod(
        private val method: Method,
        private val invokeOn: Any
) {


    fun invoke() {
        val result = method.invoke(invokeOn)
        if (result != null) {
            println("After deployment ${method.name} returned $result")
        }
    }
}