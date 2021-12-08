package com.nimbusframework.nimbuslocal.deployment.services.usesresources

import com.nimbusframework.nimbuslocal.deployment.function.FunctionEnvironment
import com.nimbusframework.nimbuslocal.deployment.function.FunctionIdentifier
import com.nimbusframework.nimbuslocal.deployment.services.LocalResourceHolder
import java.lang.reflect.Method

abstract class LocalUsesResourcesHandler(
        private val localResourceHolder: LocalResourceHolder
) {

    protected abstract fun handleUsesResources(clazz: Class<out Any>, method: Method, functionEnvironment: FunctionEnvironment)

    fun handleFunctionEnvironment(clazz: Class<out Any>, method: Method) {
        val functionIdentifier = FunctionIdentifier(clazz.name, method.name)
        val functionEnvironment = localResourceHolder.functionEnvironments[functionIdentifier]

        if (functionEnvironment != null) {
            handleUsesResources(clazz, method, functionEnvironment)
        }
    }

}