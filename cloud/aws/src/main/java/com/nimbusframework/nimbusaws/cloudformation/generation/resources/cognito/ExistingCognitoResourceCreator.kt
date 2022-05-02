package com.nimbusframework.nimbusaws.cloudformation.generation.resources.cognito

import com.nimbusframework.nimbusaws.annotation.annotations.cognito.ExistingCognitoUserPool
import com.nimbusframework.nimbusaws.annotation.annotations.cognito.ExistingCognitoUserPools
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.CloudResourceResourceCreator
import com.nimbusframework.nimbusaws.cloudformation.model.CloudFormationFiles
import com.nimbusframework.nimbuscore.persisted.NimbusState
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element

class ExistingCognitoResourceCreator(
    roundEnvironment: RoundEnvironment,
    cfDocuments: MutableMap<String, CloudFormationFiles>,
    nimbusState: NimbusState
) : CloudResourceResourceCreator(
    roundEnvironment,
    cfDocuments,
    nimbusState,
    ExistingCognitoUserPool::class.java,
    ExistingCognitoUserPools::class.java
) {

    override fun handleAgnosticType(type: Element) {
        val userPools = type.getAnnotationsByType(ExistingCognitoUserPool::class.java)

        for (userPool in userPools) {
            for (stage in stageService.determineStages(userPool.stages)) {
                val cloudFormationDocuments = cfDocuments.getOrPut(stage) { CloudFormationFiles(nimbusState, stage) }
                cloudFormationDocuments.addAdditionalAttribute(type, userPool.arn)
            }
        }
    }

}
