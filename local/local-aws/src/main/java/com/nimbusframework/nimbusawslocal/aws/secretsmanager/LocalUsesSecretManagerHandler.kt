package com.nimbusframework.nimbusawslocal.aws.secretsmanager

import com.nimbusframework.nimbusaws.annotation.annotations.secretmanager.UsesSecretManagerSecret
import com.nimbusframework.nimbusawslocal.aws.AwsPermissionTypes
import com.nimbusframework.nimbuslocal.deployment.function.FunctionEnvironment
import com.nimbusframework.nimbuslocal.deployment.function.permissions.Permission
import com.nimbusframework.nimbuslocal.deployment.services.LocalResourceHolder
import com.nimbusframework.nimbuslocal.deployment.services.StageService
import com.nimbusframework.nimbuslocal.deployment.services.usesresources.LocalUsesResourcesHandler
import java.lang.reflect.Method

class LocalUsesSecretManagerHandler(
        localResourceHolder: LocalResourceHolder,
        private val stageService: StageService
) : LocalUsesResourcesHandler(localResourceHolder) {

    override fun handleUsesResources(clazz: Class<out Any>, method: Method, functionEnvironment: FunctionEnvironment) {
        val usesSecret = method.getAnnotationsByType(UsesSecretManagerSecret::class.java)

        val annotations = stageService.annotationsForStage(usesSecret) { annotation -> annotation.stages }
        for (annotation in annotations) {
            val arn = annotation.secretArn
            functionEnvironment.addPermission(AwsPermissionTypes.SECRETS_MANAGER, object: Permission {
                override fun hasPermission(value: String): Boolean {
                    return value == arn
                }
            })
        }

    }

}
