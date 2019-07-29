package com.nimbusframework.nimbuscore.testing.services.function

import com.nimbusframework.nimbuscore.annotation.annotations.function.BasicServerlessFunction
import com.nimbusframework.nimbuscore.testing.basic.BasicFunction
import com.nimbusframework.nimbuscore.testing.function.FunctionIdentifier
import com.nimbusframework.nimbuscore.testing.function.ServerlessFunction
import com.nimbusframework.nimbuscore.testing.function.information.BasicFunctionInformation
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

                val basicMethod = BasicFunction(method, invokeOn)
                val basicFunctionInformation = BasicFunctionInformation(basicFunction.cron)
                localResourceHolder.functions[functionIdentifier] = ServerlessFunction(basicMethod, basicFunctionInformation)
                localResourceHolder.basicMethods[functionIdentifier] = basicMethod
            }
        }
        return true
    }

}