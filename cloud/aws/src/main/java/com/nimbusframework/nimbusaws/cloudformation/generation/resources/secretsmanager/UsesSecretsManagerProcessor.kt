package com.nimbusframework.nimbusaws.cloudformation.generation.resources.secretsmanager

import com.nimbusframework.nimbusaws.annotation.annotations.secretmanager.UsesSecretManagerSecret
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.UsesResourcesProcessor
import com.nimbusframework.nimbusaws.cloudformation.model.resource.ExistingResource
import com.nimbusframework.nimbusaws.cloudformation.model.resource.function.FunctionResource
import com.nimbusframework.nimbuscore.persisted.NimbusState
import javax.lang.model.element.Element

class UsesSecretsManagerProcessor(
    nimbusState: NimbusState
): UsesResourcesProcessor(nimbusState)  {

    override fun handleUseResources(serverlessMethod: Element, functionResource: FunctionResource) {
        val iamRoleResource = functionResource.getIamRoleResource()

        for (usesSecretsManagerSecret in serverlessMethod.getAnnotationsByType(UsesSecretManagerSecret::class.java)) {
            for (stage in stageService.determineStages(usesSecretsManagerSecret.stages)) {
                if (stage == functionResource.stage) {
                    iamRoleResource.addAllowStatement("secretsmanager:GetSecretValue", ExistingResource(usesSecretsManagerSecret.secretArn, nimbusState, stage), "")
                }
            }
        }

    }
}
