package com.nimbusframework.nimbuscore.testing.services.function

import com.nimbusframework.nimbuscore.annotation.annotations.deployment.AfterDeployment
import com.nimbusframework.nimbuscore.testing.deployment.AfterDeploymentMethod
import com.nimbusframework.nimbuscore.testing.services.LocalResourceHolder
import java.lang.reflect.Method

class LocalAfterDeploymentHandler(
        private val localResourceHolder: LocalResourceHolder,
        private val stage: String
) : LocalFunctionHandler(localResourceHolder) {

    override fun handleMethod(clazz: Class<out Any>, method: Method) {
        val afterDeployments = method.getAnnotationsByType(AfterDeployment::class.java)

        for (afterDeployment in afterDeployments) {
            if (afterDeployment.stages.contains(stage)) {
                val invokeOn = clazz.getConstructor().newInstance()

                if (afterDeployment.isTest) {
                    localResourceHolder.afterDeployments.addLast(AfterDeploymentMethod(method, invokeOn))
                } else {
                    localResourceHolder.afterDeployments.addFirst(AfterDeploymentMethod(method, invokeOn))
                }
            }
        }
    }

}