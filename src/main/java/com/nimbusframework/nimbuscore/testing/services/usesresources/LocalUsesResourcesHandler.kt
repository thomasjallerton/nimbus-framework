package com.nimbusframework.nimbuscore.testing.services.usesresources

import com.nimbusframework.nimbuscore.testing.function.FunctionEnvironment
import com.nimbusframework.nimbuscore.testing.function.FunctionIdentifier
import com.nimbusframework.nimbuscore.testing.services.LocalResourceHolder
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