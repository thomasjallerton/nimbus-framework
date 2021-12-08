package com.nimbusframework.nimbuslocal.deployment.services.function

import com.nimbusframework.nimbuscore.annotations.function.BasicServerlessFunction
import com.nimbusframework.nimbuslocal.deployment.basic.BasicFunction
import com.nimbusframework.nimbuslocal.deployment.function.FunctionIdentifier
import com.nimbusframework.nimbuslocal.deployment.function.ServerlessFunction
import com.nimbusframework.nimbuslocal.deployment.function.information.BasicFunctionInformation
import com.nimbusframework.nimbuslocal.deployment.services.LocalResourceHolder
import com.nimbusframework.nimbuslocal.deployment.services.StageService
import java.lang.reflect.Method

class LocalBasicFunctionHandler(
        private val localResourceHolder: LocalResourceHolder,
        private val stageService: StageService
) : LocalFunctionHandler(localResourceHolder) {
    override fun handleMethod(clazz: Class<out Any>, method: Method): Boolean {

        val basicServerlessFunctions = method.getAnnotationsByType(BasicServerlessFunction::class.java)
        if (basicServerlessFunctions.isEmpty()) return false

        val functionIdentifier = FunctionIdentifier(clazz.canonicalName, method.name)

        val annotation = stageService.annotationForStage(basicServerlessFunctions) {annotation -> annotation.stages}
        if (annotation != null) {
            val invokeOn = getFunctionClassInstance(clazz)

            val basicMethod = BasicFunction(method, invokeOn)
            val basicFunctionInformation = BasicFunctionInformation(annotation.cron)
            localResourceHolder.functions[functionIdentifier] = ServerlessFunction(basicMethod, basicFunctionInformation)
            localResourceHolder.basicMethods[functionIdentifier] = basicMethod
        }
        return true
    }

}