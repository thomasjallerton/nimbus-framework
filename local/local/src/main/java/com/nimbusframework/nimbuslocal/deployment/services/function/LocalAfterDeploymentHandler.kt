package com.nimbusframework.nimbuslocal.deployment.services.function

import com.nimbusframework.nimbuscore.annotations.deployment.AfterDeployment
import com.nimbusframework.nimbuslocal.deployment.afterdeployment.AfterDeploymentMethod
import com.nimbusframework.nimbuslocal.deployment.services.LocalResourceHolder
import com.nimbusframework.nimbuslocal.deployment.services.StageService
import java.lang.reflect.Method

class LocalAfterDeploymentHandler(
        private val localResourceHolder: LocalResourceHolder,
        private val stageService: StageService
) : LocalFunctionHandler(localResourceHolder) {

    override fun handleMethod(clazz: Class<out Any>, method: Method): Boolean {
        val afterDeployments = method.getAnnotationsByType(AfterDeployment::class.java)
        if (afterDeployments.isEmpty()) return false

        val annotation = stageService.annotationForStage(afterDeployments) {annotation -> annotation.stages}
        if (annotation != null) {
            val invokeOn = getFunctionClassInstance(clazz)

            if (annotation.isTest) {
                localResourceHolder.afterDeployments.addLast(AfterDeploymentMethod(method, invokeOn))
            } else {
                localResourceHolder.afterDeployments.addFirst(AfterDeploymentMethod(method, invokeOn))
            }
        }
        return true
    }

}