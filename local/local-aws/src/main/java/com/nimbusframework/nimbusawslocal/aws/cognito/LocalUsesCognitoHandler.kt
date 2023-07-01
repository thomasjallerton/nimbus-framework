package com.nimbusframework.nimbusawslocal.aws.cognito

import com.nimbusframework.nimbusaws.annotation.annotations.cognito.CognitoArnAnnotationService
import com.nimbusframework.nimbusaws.annotation.annotations.cognito.UsesCognitoUserPool
import com.nimbusframework.nimbusawslocal.aws.AwsPermissionTypes
import com.nimbusframework.nimbuscore.clients.queue.QueueIdAnnotationService
import com.nimbusframework.nimbuslocal.deployment.function.FunctionEnvironment
import com.nimbusframework.nimbuslocal.deployment.function.permissions.Permission
import com.nimbusframework.nimbuslocal.deployment.services.LocalResourceHolder
import com.nimbusframework.nimbuslocal.deployment.services.StageService
import com.nimbusframework.nimbuslocal.deployment.services.usesresources.LocalUsesResourcesHandler
import java.lang.reflect.Method

class LocalUsesCognitoHandler(
        localResourceHolder: LocalResourceHolder,
        private val stageService: StageService
) : LocalUsesResourcesHandler(localResourceHolder) {

    override fun handleUsesResources(clazz: Class<out Any>, method: Method, functionEnvironment: FunctionEnvironment) {
        val usesCognito = method.getAnnotationsByType(UsesCognitoUserPool::class.java)

        val annotations = stageService.annotationsForStage(usesCognito) { annotation -> annotation.stages}
        for (annotation in annotations) {
            val arn = CognitoArnAnnotationService.getArn(annotation.userPool.java, stageService.deployingStage)
            functionEnvironment.addPermission(AwsPermissionTypes.COGNITO, object: Permission {
                override fun hasPermission(value: String): Boolean {
                    return value == arn
                }
            })
        }

        val usesCognitoAsAdmin = method.getAnnotationsByType(UsesCognitoUserPool::class.java)
        val adminAnnotations = stageService.annotationsForStage(usesCognitoAsAdmin) { annotation -> annotation.stages}
        for (adminAnnotation in adminAnnotations) {
            val arn = CognitoArnAnnotationService.getArn(adminAnnotation.userPool.java, stageService.deployingStage)
            functionEnvironment.addPermission(AwsPermissionTypes.COGNITO_ADMIN, object: Permission {
                override fun hasPermission(value: String): Boolean {
                    return value == arn
                }
            })
        }
    }

}
