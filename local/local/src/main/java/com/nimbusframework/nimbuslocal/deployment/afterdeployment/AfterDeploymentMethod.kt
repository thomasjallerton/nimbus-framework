package com.nimbusframework.nimbuslocal.deployment.afterdeployment

import com.nimbusframework.nimbuscore.eventabstractions.BasicEvent
import com.nimbusframework.nimbuslocal.ServerlessMethod
import com.nimbusframework.nimbuslocal.deployment.function.FunctionType
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