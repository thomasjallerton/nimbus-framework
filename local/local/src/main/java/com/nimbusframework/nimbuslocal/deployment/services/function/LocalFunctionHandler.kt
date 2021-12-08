package com.nimbusframework.nimbuslocal.deployment.services.function

import com.nimbusframework.nimbuscore.annotations.deployment.CustomFactory
import com.nimbusframework.nimbuslocal.deployment.function.FunctionEnvironment
import com.nimbusframework.nimbuslocal.deployment.function.FunctionIdentifier
import com.nimbusframework.nimbuslocal.deployment.services.LocalResourceHolder
import java.lang.reflect.Method
import kotlin.reflect.full.primaryConstructor

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

    protected fun getFunctionClassInstance(clazz: Class<out Any>): Any {
        val factoryClass = clazz.getAnnotation(CustomFactory::class.java)?.value
            ?: return clazz.getConstructor().newInstance()

        return factoryClass.java.getConstructor().newInstance().create()
    }

}