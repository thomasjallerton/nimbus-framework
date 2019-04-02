package annotation.services.useresources

import annotation.annotations.websocket.UsesServerlessFunctionWebSocketClient
import cloudformation.CloudFormationDocuments
import cloudformation.resource.function.FunctionResource
import javax.lang.model.element.Element

class UsesServerlessFunctionWebSocketClientHandler(
        private val cfDocuments: Map<String, CloudFormationDocuments>
): UsesResourcesHandler  {

    override fun handleUseResources(serverlessMethod: Element, functionResource: FunctionResource) {
        val iamRoleResource = functionResource.getIamRoleResource()

        for (webSocketClient in serverlessMethod.getAnnotationsByType(UsesServerlessFunctionWebSocketClient::class.java)) {
            for (stage in webSocketClient.stages) {
                if (stage == functionResource.stage) {

                    val webSocketApi = cfDocuments.getValue(stage).rootWebSocketApi
                    if (webSocketApi != null) {
                        functionResource.addEnvVariable("WEBSOCKET_ENDPOINT", webSocketApi.getEndpoint())

                        iamRoleResource.addAllowStatement("execute-api:ManageConnections", webSocketApi, "/*")
                    }
                }
            }
        }
    }
}