package annotation.services.useresources

import annotation.annotations.function.UsesBasicServerlessFunctionClient
import cloudformation.CloudFormationDocuments
import cloudformation.resource.function.FunctionResource
import persisted.NimbusState
import javax.lang.model.element.Element

class UsesBasicServerlessFunctionClientHandler(
        private val cfDocuments: Map<String, CloudFormationDocuments>,
        private val nimbusState: NimbusState
): UsesResourcesHandler {

    override fun handleUseResources(serverlessMethod: Element, functionResource: FunctionResource) {
        for (usesBasicServerlessFunctionClient in serverlessMethod.getAnnotationsByType(UsesBasicServerlessFunctionClient::class.java)) {
            for (stage in usesBasicServerlessFunctionClient.stages) {
                if (stage == functionResource.stage) {

                    functionResource.addEnvVariable("NIMBUS_PROJECT_NAME", nimbusState.projectName)
                    functionResource.addEnvVariable("FUNCTION_STAGE", stage)
                    val updateResources = cfDocuments.getValue(stage).updateResources
                    val invokableFunctions = updateResources.getInvokableFunctions()
                    for (invokableFunction in invokableFunctions) {
                        functionResource.getIamRoleResource().addAllowStatement("lambda:*", invokableFunction, "")
                    }
                }
            }
        }
    }
}