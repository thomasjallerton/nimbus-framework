package com.nimbusframework.nimbuscore.annotation.services.useresources

import com.nimbusframework.nimbuscore.annotation.annotations.websocket.UsesServerlessFunctionWebSocketClient
import com.nimbusframework.nimbuscore.cloudformation.CloudFormationDocuments
import com.nimbusframework.nimbuscore.cloudformation.resource.function.FunctionResource
import com.nimbusframework.nimbuscore.persisted.ClientType
import javax.lang.model.element.Element

class UsesServerlessFunctionWebSocketClientHandler(
        private val cfDocuments: Map<String, CloudFormationDocuments>
): UsesResourcesHandler  {

    override fun handleUseResources(serverlessMethod: Element, functionResource: FunctionResource) {
        val iamRoleResource = functionResource.getIamRoleResource()

        for (webSocketClient in serverlessMethod.getAnnotationsByType(UsesServerlessFunctionWebSocketClient::class.java)) {
            functionResource.addClient(ClientType.WebSocket)

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