package com.nimbusframework.nimbuscore.testing.services.function

import com.nimbusframework.nimbuscore.annotation.annotations.function.KeyValueStoreServerlessFunction
import com.nimbusframework.nimbuscore.clients.keyvalue.AbstractKeyValueStoreClient
import com.nimbusframework.nimbuscore.testing.LocalNimbusDeployment
import com.nimbusframework.nimbuscore.testing.document.KeyValueMethod
import com.nimbusframework.nimbuscore.testing.function.FunctionIdentifier
import com.nimbusframework.nimbuscore.testing.function.ServerlessFunction
import com.nimbusframework.nimbuscore.testing.function.information.KeyValueStoreFunctionInformation
import com.nimbusframework.nimbuscore.testing.services.LocalResourceHolder
import java.lang.reflect.Method

class LocalKeyValueStoreFunctionHandler(
        private val localResourceHolder: LocalResourceHolder,
        private val stage: String
) : LocalFunctionHandler(localResourceHolder) {

    override fun handleMethod(clazz: Class<out Any>, method: Method): Boolean {
        val functionIdentifier = FunctionIdentifier(clazz.canonicalName, method.name)

        val keyValueFunctions = method.getAnnotationsByType(KeyValueStoreServerlessFunction::class.java)
        if (keyValueFunctions.isEmpty()) return false

        for (keyValueFunction in keyValueFunctions) {
            if (keyValueFunction.stages.contains(stage)) {
                val invokeOn = clazz.getConstructor().newInstance()

                val keyValueMethod = KeyValueMethod(method, invokeOn, keyValueFunction.method)
                val functionInformation = KeyValueStoreFunctionInformation(
                        keyValueFunction.dataModel.simpleName ?: "",
                        keyValueFunction.method
                )
                localResourceHolder.functions[functionIdentifier] = ServerlessFunction(keyValueMethod, functionInformation)
                val tableName = AbstractKeyValueStoreClient.getTableName(keyValueFunction.dataModel.java, LocalNimbusDeployment.stage)
                val keyValueStore = localResourceHolder.keyValueStores[tableName]
                keyValueStore?.addMethod(keyValueMethod)
            }
        }
        return true
    }
}