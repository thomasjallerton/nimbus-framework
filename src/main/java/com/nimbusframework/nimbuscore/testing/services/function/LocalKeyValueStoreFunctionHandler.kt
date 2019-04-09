package com.nimbusframework.nimbuscore.testing.services.function

import com.nimbusframework.nimbuscore.annotation.annotations.function.KeyValueStoreServerlessFunction
import com.nimbusframework.nimbuscore.clients.keyvalue.AbstractKeyValueStoreClient
import com.nimbusframework.nimbuscore.testing.LocalNimbusDeployment
import com.nimbusframework.nimbuscore.testing.document.KeyValueMethod
import com.nimbusframework.nimbuscore.testing.function.FunctionIdentifier
import com.nimbusframework.nimbuscore.testing.services.LocalResourceHolder
import java.lang.reflect.Method

class LocalKeyValueStoreFunctionHandler(
        private val localResourceHolder: LocalResourceHolder,
        private val stage: String
) : LocalFunctionHandler(localResourceHolder) {

    override fun handleMethod(clazz: Class<out Any>, method: Method) {
        val functionIdentifier = FunctionIdentifier(clazz.canonicalName, method.name)

        val keyValueFunctions = method.getAnnotationsByType(KeyValueStoreServerlessFunction::class.java)

        for (keyValueFunction in keyValueFunctions) {
            if (keyValueFunction.stages.contains(stage)) {
                val invokeOn = clazz.getConstructor().newInstance()

                val documentMethod = KeyValueMethod(method, invokeOn, keyValueFunction.method)
                localResourceHolder.methods[functionIdentifier] = documentMethod
                val tableName = AbstractKeyValueStoreClient.getTableName(keyValueFunction.dataModel.java, LocalNimbusDeployment.stage)
                val keyValueStore = localResourceHolder.keyValueStores[tableName]
                keyValueStore?.addMethod(documentMethod)
            }
        }
    }
}