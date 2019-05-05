package com.nimbusframework.nimbuscore.testing.services.function

import com.nimbusframework.nimbuscore.annotation.annotations.function.BasicServerlessFunction
import com.nimbusframework.nimbuscore.testing.basic.BasicMethod
import com.nimbusframework.nimbuscore.testing.function.FunctionIdentifier
import com.nimbusframework.nimbuscore.testing.services.LocalResourceHolder
import java.lang.reflect.Method

class LocalBasicFunctionHandler(
        private val localResourceHolder: LocalResourceHolder,
        private val stage: String
) : LocalFunctionHandler(localResourceHolder) {
    override fun handleMethod(clazz: Class<out Any>, method: Method): Boolean {

        val functionIdentifier = FunctionIdentifier(clazz.canonicalName, method.name)

        val basicServerlessFunctions = method.getAnnotationsByType(BasicServerlessFunction::class.java)
        if (basicServerlessFunctions.isEmpty()) return false

        for (basicFunction in basicServerlessFunctions) {
            if (basicFunction.stages.contains(stage)) {
                val invokeOn = clazz.getConstructor().newInstance()

                val basicMethod = BasicMethod(method, invokeOn)
                localResourceHolder.methods[functionIdentifier] = basicMethod
                localResourceHolder.basicMethods[functionIdentifier] = basicMethod
            }
        }
        return true
    }

}