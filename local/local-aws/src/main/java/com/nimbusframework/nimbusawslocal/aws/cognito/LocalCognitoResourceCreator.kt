package com.nimbusframework.nimbusawslocal.aws.cognito

import com.nimbusframework.nimbusaws.annotation.annotations.cognito.ExistingCognitoUserPool
import com.nimbusframework.nimbusawslocal.aws.LocalAwsResourceHolder
import com.nimbusframework.nimbuslocal.deployment.services.StageService
import com.nimbusframework.nimbuslocal.deployment.services.resource.LocalCreateResourcesHandler

class LocalCognitoResourceCreator(
    private val stageService: StageService,
    private val resourceHolder: LocalAwsResourceHolder
): LocalCreateResourcesHandler {

    override fun createResource(clazz: Class<*>) {
        val cognitoUserPoolAnnotations = clazz.getAnnotationsByType(ExistingCognitoUserPool::class.java)

        val annotationForStage = stageService.annotationForStage(cognitoUserPoolAnnotations) { userPool -> userPool.stages }
        if (annotationForStage != null) {
            val localCognito = LocalCognito(annotationForStage.arn)
            resourceHolder.cognitoUserPools[clazz] = localCognito
        }
    }

}
