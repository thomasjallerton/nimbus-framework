package com.nimbusframework.nimbuslocal.deployment.services.function

import com.nimbusframework.nimbuscore.annotations.function.KeyValueStoreServerlessFunction
import com.nimbusframework.nimbuslocal.deployment.function.FunctionIdentifier
import com.nimbusframework.nimbuslocal.deployment.function.ServerlessFunction
import com.nimbusframework.nimbuslocal.deployment.function.information.KeyValueStoreFunctionInformation
import com.nimbusframework.nimbuslocal.deployment.keyvalue.KeyValueMethod
import com.nimbusframework.nimbuslocal.deployment.services.LocalResourceHolder
import com.nimbusframework.nimbuslocal.deployment.services.StageService
import java.lang.reflect.Method

class LocalKeyValueStoreFunctionHandler(
        private val localResourceHolder: LocalResourceHolder,
        private val stageService: StageService
) : LocalFunctionHandler(localResourceHolder) {

    override fun handleMethod(clazz: Class<out Any>, method: Method): Boolean {
        val keyValueFunctions = method.getAnnotationsByType(KeyValueStoreServerlessFunction::class.java)
        if (keyValueFunctions.isEmpty()) return false

        val functionIdentifier = FunctionIdentifier(clazz.canonicalName, method.name)

        val annotation = stageService.annotationForStage(keyValueFunctions) {annotation -> annotation.stages}
        if (annotation != null) {
            val invokeOn = getFunctionClassInstance(clazz)

            val keyValueMethod = KeyValueMethod(method, invokeOn, annotation.method)
            val functionInformation = KeyValueStoreFunctionInformation(
                    annotation.dataModel.simpleName ?: "",
                    annotation.method
            )
            localResourceHolder.functions[functionIdentifier] = ServerlessFunction(keyValueMethod, functionInformation)
            val keyValueStore = localResourceHolder.keyValueStores[annotation.dataModel.java]
            keyValueStore?.addMethod(keyValueMethod)
        }
        return true
    }
}