package com.nimbusframework.nimbuscore.testing.services.function

import com.nimbusframework.nimbuscore.annotation.annotations.function.DocumentStoreServerlessFunction
import com.nimbusframework.nimbuscore.clients.document.AbstractDocumentStoreClient
import com.nimbusframework.nimbuscore.testing.LocalNimbusDeployment
import com.nimbusframework.nimbuscore.testing.document.DocumentMethod
import com.nimbusframework.nimbuscore.testing.function.FunctionIdentifier
import com.nimbusframework.nimbuscore.testing.services.LocalResourceHolder
import java.lang.reflect.Method

class LocalDocumentStoreFunctionHandler(
        private val localResourceHolder: LocalResourceHolder,
        private val stage: String
) : LocalFunctionHandler(localResourceHolder) {

    override fun handleMethod(clazz: Class<out Any>, method: Method): Boolean {
        val functionIdentifier = FunctionIdentifier(clazz.canonicalName, method.name)

        val documentFunctions = method.getAnnotationsByType(DocumentStoreServerlessFunction::class.java)
        if (documentFunctions.isEmpty()) return false

        for (documentFunction in documentFunctions) {
            if (documentFunction.stages.contains(stage)) {
                val invokeOn = clazz.getConstructor().newInstance()

                val documentMethod = DocumentMethod(method, invokeOn, documentFunction.method)
                localResourceHolder.methods[functionIdentifier] = documentMethod
                val tableName = AbstractDocumentStoreClient.getTableName(documentFunction.dataModel.java, LocalNimbusDeployment.stage)
                val documentStore = localResourceHolder.documentStores[tableName]
                documentStore?.addMethod(documentMethod)
            }
        }
        return true
    }
}