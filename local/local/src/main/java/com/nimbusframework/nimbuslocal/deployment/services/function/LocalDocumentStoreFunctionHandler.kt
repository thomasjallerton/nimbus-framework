package com.nimbusframework.nimbuslocal.deployment.services.function

import com.nimbusframework.nimbuscore.annotations.function.DocumentStoreServerlessFunction
import com.nimbusframework.nimbuslocal.deployment.document.DocumentMethod
import com.nimbusframework.nimbuslocal.deployment.function.FunctionIdentifier
import com.nimbusframework.nimbuslocal.deployment.function.ServerlessFunction
import com.nimbusframework.nimbuslocal.deployment.function.information.DocumentStoreFunctionInformation
import com.nimbusframework.nimbuslocal.deployment.services.LocalResourceHolder
import com.nimbusframework.nimbuslocal.deployment.services.StageService
import java.lang.reflect.Method

class LocalDocumentStoreFunctionHandler(
        private val localResourceHolder: LocalResourceHolder,
        private val stageService: StageService
) : LocalFunctionHandler(localResourceHolder) {

    override fun handleMethod(clazz: Class<out Any>, method: Method): Boolean {
        val documentFunctions = method.getAnnotationsByType(DocumentStoreServerlessFunction::class.java)
        if (documentFunctions.isEmpty()) return false

        val functionIdentifier = FunctionIdentifier(clazz.canonicalName, method.name)

        val annotation = stageService.annotationForStage(documentFunctions) {annotation -> annotation.stages}
        if (annotation != null) {
            val invokeOn = getFunctionClassInstance(clazz)

            val documentMethod = DocumentMethod(method, invokeOn, annotation.method)
            val functionInformation = DocumentStoreFunctionInformation(
                    annotation.dataModel.simpleName ?: "",
                    annotation.method
            )
            localResourceHolder.functions[functionIdentifier] = ServerlessFunction(documentMethod, functionInformation)
            val documentStore = localResourceHolder.documentStores[annotation.dataModel.java]
            documentStore?.addMethod(documentMethod)
        }
        return true
    }
}