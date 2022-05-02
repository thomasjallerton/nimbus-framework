package com.nimbusframework.nimbusaws.cloudformation.generation.resources.apigateway

import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.UsesResourcesProcessor
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.environment.ConstantEnvironmentVariable
import com.nimbusframework.nimbusaws.cloudformation.model.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.model.resource.function.FunctionResource
import com.nimbusframework.nimbuscore.annotations.websocket.UsesServerlessFunctionWebSocket
import com.nimbusframework.nimbuscore.persisted.NimbusState
import javax.lang.model.element.Element

class UsesServerlessFunctionWebSocketClientProcessor(
    private val cfDocuments: Map<String, CloudFormationFiles>,
    nimbusState: NimbusState
): UsesResourcesProcessor(nimbusState)  {

    override fun handleUseResources(serverlessMethod: Element, functionResource: FunctionResource) {
        val iamRoleResource = functionResource.getIamRoleResource()

        for (webSocketClient in serverlessMethod.getAnnotationsByType(UsesServerlessFunctionWebSocket::class.java)) {

            for (stage in stageService.determineStages(webSocketClient.stages)) {
                if (stage == functionResource.stage) {

                    val webSocketApi = cfDocuments.getValue(stage).updateTemplate.rootWebSocketApi
                    if (webSocketApi != null) {
                        functionResource.addEnvVariable(ConstantEnvironmentVariable.WEBSOCKET_ENDPOINT, webSocketApi.getEndpoint())

                        iamRoleResource.addAllowStatement("execute-api:ManageConnections", webSocketApi, "/*")
                    }
                }
            }
        }
    }
}
