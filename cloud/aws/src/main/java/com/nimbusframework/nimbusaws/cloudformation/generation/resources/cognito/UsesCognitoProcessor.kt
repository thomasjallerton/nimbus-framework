package com.nimbusframework.nimbusaws.cloudformation.generation.resources.cognito

import com.nimbusframework.nimbusaws.annotation.annotations.cognito.UsesCognitoUserPool
import com.nimbusframework.nimbusaws.annotation.annotations.cognito.UsesCognitoUserPoolAsAdmin
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.UsesResourcesProcessor
import com.nimbusframework.nimbusaws.cloudformation.model.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.model.resource.ExistingResource
import com.nimbusframework.nimbusaws.cloudformation.model.resource.function.FunctionResource
import com.nimbusframework.nimbuscore.persisted.NimbusState
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.tools.Diagnostic

class UsesCognitoProcessor(
    private val cfDocuments: Map<String, CloudFormationFiles>,
    private val processingEnv: ProcessingEnvironment,
    private val messager: Messager,
    nimbusState: NimbusState
): UsesResourcesProcessor(nimbusState)  {

    override fun handleUseResources(serverlessMethod: Element, functionResource: FunctionResource) {
        val iamRoleResource = functionResource.iamRoleResource

        for (usesCognitoUserPool in serverlessMethod.getAnnotationsByType(UsesCognitoUserPool::class.java)) {

            for (stage in stageService.determineStages(usesCognitoUserPool.stages)) {
                if (stage == functionResource.stage) {
                    val cloudFormationDocuments = cfDocuments[stage]!!
                    val typeElem = UsesCognitoUserPoolAnnotation(usesCognitoUserPool).getTypeElement(processingEnv)
                    val arn = cloudFormationDocuments.getAdditionalAttribute(typeElem)
                    if (arn == null) {
                        messager.printMessage(Diagnostic.Kind.ERROR, "Unable to find cognito user pool for class ${typeElem.qualifiedName} in stage $stage", serverlessMethod)
                        return
                    }
                    iamRoleResource.addAllowStatement("cognito-idp:GetUser", ExistingResource(arn, nimbusState, stage), "")
                    iamRoleResource.addAllowStatement("cognito-idp:ListUsers", ExistingResource(arn, nimbusState, stage), "")
                }
            }
        }

        for (usesCognitoUserPool in serverlessMethod.getAnnotationsByType(UsesCognitoUserPoolAsAdmin::class.java)) {
            for (stage in stageService.determineStages(usesCognitoUserPool.stages)) {
                if (stage == functionResource.stage) {
                    val cloudFormationDocuments = cfDocuments[stage]!!
                    val arn = cloudFormationDocuments.getAdditionalAttribute(UsesCognitoUserPoolAsAdminAnnotation(usesCognitoUserPool).getTypeElement(processingEnv))
                    if (arn == null) {
                        messager.printMessage(Diagnostic.Kind.ERROR, "Unable to find cognito user pool for class ${usesCognitoUserPool.userPool.simpleName} in stage $stage", serverlessMethod)
                        return
                    }
                    iamRoleResource.addAllowStatement("cognito-idp:AdminAddUserToGroup", ExistingResource(arn, nimbusState, stage), "")
                    iamRoleResource.addAllowStatement("cognito-idp:AdminRemoveUserFromGroup", ExistingResource(arn, nimbusState, stage), "")
                    iamRoleResource.addAllowStatement("cognito-idp:AdminUpdateUserAttributes", ExistingResource(arn, nimbusState, stage), "")
                }
            }
        }
    }
}
