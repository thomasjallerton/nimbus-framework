package com.nimbusframework.nimbuscore.testing.services.function

import com.nimbusframework.nimbuscore.testing.function.FunctionEnvironment
import com.nimbusframework.nimbuscore.testing.function.FunctionIdentifier
import com.nimbusframework.nimbuscore.testing.services.LocalResourceHolder
import java.lang.reflect.Method

abstract class LocalFunctionHandler(
        private val localResourceHolder: LocalResourceHolder
) {

    protected abstract fun handleMethod(clazz: Class<out Any>, method: Method): Boolean

    fun createLocalFunctions(clazz: Class<out Any>, method: Method) {
        val functionIdentifier = FunctionIdentifier(clazz.name, method.name)
        if (handleMethod(clazz, method)) {
            if (!localResourceHolder.functionEnvironments.containsKey(functionIdentifier)) {
                localResourceHolder.functionEnvironments[functionIdentifier] = FunctionEnvironment()
            }
        }
    }

}