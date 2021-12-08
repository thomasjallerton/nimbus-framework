package com.nimbusframework.nimbuslocal.deployment.services.usesresources

import com.nimbusframework.nimbuscore.annotations.function.UsesBasicServerlessFunction
import com.nimbusframework.nimbuscore.permissions.PermissionType
import com.nimbusframework.nimbuslocal.deployment.function.FunctionEnvironment
import com.nimbusframework.nimbuslocal.deployment.function.permissions.BasicFunctionPermission
import com.nimbusframework.nimbuslocal.deployment.services.LocalResourceHolder
import com.nimbusframework.nimbuslocal.deployment.services.StageService
import java.lang.reflect.Method

class LocalUsesBasicFunctionHandler(
        localResourceHolder: LocalResourceHolder,
        private val stageService: StageService
): LocalUsesResourcesHandler(localResourceHolder) {

    override fun handleUsesResources(clazz: Class<out Any>, method: Method, functionEnvironment: FunctionEnvironment) {
        val usesBasicFunctionClients = method.getAnnotationsByType(UsesBasicServerlessFunction::class.java)

        val annotation = stageService.annotationForStage(usesBasicFunctionClients) { annotation -> annotation.stages}
        if (annotation != null) {
            functionEnvironment.addPermission(PermissionType.BASIC_FUNCTION, BasicFunctionPermission(
                    annotation.targetClass.java,
                    annotation.methodName
            ))
        }
    }

}