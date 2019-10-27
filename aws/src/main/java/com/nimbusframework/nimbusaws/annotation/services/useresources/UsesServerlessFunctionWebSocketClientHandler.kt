package com.nimbusframework.nimbusaws.annotation.services.useresources

import com.nimbusframework.nimbuscore.annotations.websocket.UsesServerlessFunctionWebSocket
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.resource.function.FunctionResource
import com.nimbusframework.nimbuscore.persisted.ClientType
import javax.lang.model.element.Element

class UsesServerlessFunctionWebSocketClientHandler(
        private val cfDocuments: Map<String, CloudFormationFiles>
): UsesResourcesHandler  {

    override fun handleUseResources(serverlessMethod: Element, functionResource: FunctionResource) {
        val iamRoleResource = functionResource.getIamRoleResource()

        for (webSocketClient in serverlessMethod.getAnnotationsByType(UsesServerlessFunctionWebSocket::class.java)) {
            functionResource.addClient(ClientType.WebSocket)

            for (stage in webSocketClient.stages) {
                if (stage == functionResource.stage) {

                    val webSocketApi = cfDocuments.getValue(stage).updateTemplate.rootWebSocketApi
                    if (webSocketApi != null) {
                        functionResource.addEnvVariable("WEBSOCKET_ENDPOINT", webSocketApi.getEndpoint())

                        iamRoleResource.addAllowStatement("execute-api:ManageConnections", webSocketApi, "/*")
                    }
                }
            }
        }
    }
}