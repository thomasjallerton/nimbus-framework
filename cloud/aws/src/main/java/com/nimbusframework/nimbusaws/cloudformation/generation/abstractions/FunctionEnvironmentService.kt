package com.nimbusframework.nimbusaws.cloudformation.generation.abstractions

import com.nimbusframework.nimbuscore.annotations.function.HttpServerlessFunction
import com.nimbusframework.nimbusaws.cloudformation.model.outputs.WebSocketApiOutput
import com.nimbusframework.nimbusaws.cloudformation.model.processing.FileBuilderMethodInformation
import com.nimbusframework.nimbusaws.cloudformation.model.resource.NimbusBucketResource
import com.nimbusframework.nimbusaws.cloudformation.model.resource.Resource
import com.nimbusframework.nimbusaws.cloudformation.model.resource.basic.CronRule
import com.nimbusframework.nimbusaws.cloudformation.model.resource.dynamo.DynamoStreamResource
import com.nimbusframework.nimbusaws.cloudformation.model.resource.function.FunctionConfig
import com.nimbusframework.nimbusaws.cloudformation.model.resource.function.FunctionEventMappingResource
import com.nimbusframework.nimbusaws.cloudformation.model.resource.function.FunctionPermissionResource
import com.nimbusframework.nimbusaws.cloudformation.model.resource.function.FunctionResource
import com.google.gson.JsonObject
import com.nimbusframework.nimbusaws.annotation.processor.ProcessingData
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.apigateway.HttpFunctionResourceCreator.Companion.getAllowedHeaders
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.apigateway.HttpFunctionResourceCreator.Companion.getAllowedOrigin
import com.nimbusframework.nimbuscore.annotations.http.HttpMethod
import com.nimbusframework.nimbusaws.cloudformation.model.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.model.resource.http.AbstractRestResource
import com.nimbusframework.nimbusaws.cloudformation.model.resource.http.CorsRestMethod
import com.nimbusframework.nimbusaws.cloudformation.model.resource.http.RestApiResource
import com.nimbusframework.nimbusaws.cloudformation.model.resource.http.RestMethod
import com.nimbusframework.nimbusaws.cloudformation.model.resource.websocket.*
import com.nimbusframework.nimbuscore.persisted.ExportInformation
import com.nimbusframework.nimbuscore.persisted.HandlerInformation

class FunctionEnvironmentService(
    private val cloudFormationFiles: MutableMap<String, CloudFormationFiles>,
    private val processingData: ProcessingData
) {

    private val nimbusState = processingData.nimbusState

    fun newFunction(fileBuilderMethodInformation: FileBuilderMethodInformation, handlerInformation: HandlerInformation, functionConfig: FunctionConfig): FunctionResource {
        val function = FunctionResource(fileBuilderMethodInformation, functionConfig, handlerInformation, nimbusState)

        val bucket = NimbusBucketResource(nimbusState, functionConfig.stage)

        val cloudFormationDocuments = cloudFormationFiles.getOrPut(functionConfig.stage) { CloudFormationFiles(
                nimbusState,
                functionConfig.stage
        ) }
        val updateResources = cloudFormationDocuments.updateTemplate.resources
        val createResources = cloudFormationDocuments.createTemplate.resources

        updateResources.addFunction(function)
        updateResources.addResource(bucket)

        createResources.addResource(bucket)

        return function
    }

    fun newHttpMethod(httpFunction: HttpServerlessFunction, function: FunctionResource) {
        val pathParts = httpFunction.path.split("/")

        val updateTemplate = cloudFormationFiles[function.stage]!!.updateTemplate
        val updateResources = updateTemplate.resources

        val restApi = updateTemplate.getOrCreateRootRestApi()

        val apiGatewayDeployment = updateTemplate.getOrCreateRootRestApiDeployment()

        var resource: AbstractRestResource = restApi

        for (part in pathParts) {
            if (part.isNotEmpty()) {
                resource = RestApiResource(resource, part, nimbusState)
                updateResources.addResource(resource)
            }
        }

        val method = httpFunction.method.name
        val restMethod = RestMethod(resource, method, mapOf(), function, updateTemplate.getRestApiAuthorizer(), nimbusState)
        apiGatewayDeployment.addDependsOn(restMethod)
        updateResources.addResource(restMethod)

        if (httpFunction.method != HttpMethod.OPTIONS && httpFunction.method != HttpMethod.ANY) {
            val allowedHeaders = getAllowedHeaders(function.stage, processingData, httpFunction)
            val allowedOrigin = getAllowedOrigin(function.stage, processingData, httpFunction)

            if (allowedHeaders.isNotEmpty() || allowedOrigin.isNotBlank()) {
                val newCorsMethod = CorsRestMethod(
                        resource,
                        updateTemplate,
                        nimbusState
                )
                val existingCorsMethod = updateResources.get(newCorsMethod.getName())
                val corsMethod = if (existingCorsMethod == null) {
                    apiGatewayDeployment.addDependsOn(newCorsMethod)
                    updateResources.addResource(newCorsMethod)
                    newCorsMethod
                } else {
                    existingCorsMethod as CorsRestMethod
                }
                corsMethod.addHeaders(allowedHeaders)
                corsMethod.addOrigin(allowedOrigin)
                corsMethod.addMethod(httpFunction.method)
            }
        }

        val permission = FunctionPermissionResource(function, restApi, nimbusState)
        updateResources.addResource(permission)
    }


    fun newStoreTrigger(store: Resource, function: FunctionResource) {
        val cfDocuments = cloudFormationFiles[function.stage]!!
        val updateResources = cfDocuments.updateTemplate.resources

        val eventMapping = FunctionEventMappingResource(
                store.getAttribute("StreamArn"),
                store.getName(),
                1,
                function,
                true,
                nimbusState
        )

        updateResources.addResource(eventMapping)

        val streamSpecification = JsonObject()
        streamSpecification.addProperty("StreamViewType", "NEW_AND_OLD_IMAGES")
        store.addExtraProperty("StreamSpecification", streamSpecification)

        val dynamoStreamResource = DynamoStreamResource(store, nimbusState)

        function.iamRoleResource.addAllowStatement("dynamodb:*", dynamoStreamResource, "")
    }

    fun newCronTrigger(cron: String, function: FunctionResource) {
        val cfDocuments = cloudFormationFiles[function.stage]!!
        val updateResources = cfDocuments.updateTemplate.resources

        val cronRule = CronRule(cron, function, nimbusState)
        val lambdaPermissionResource = FunctionPermissionResource(function, cronRule, nimbusState)

        updateResources.addResource(cronRule)
        updateResources.addResource(lambdaPermissionResource)
    }

    fun newWebSocketRoute(routeKey: String, function: FunctionResource) {
        val updateTemplate = cloudFormationFiles[function.stage]!!.updateTemplate
        val updateResources = updateTemplate.resources
        val updateOutputs = updateTemplate.outputs

        val webSocketApi = if (updateTemplate.rootWebSocketApi == null) {
            val webSocketApi = WebSocketApi(nimbusState, function.stage)
            updateTemplate.rootWebSocketApi = webSocketApi
            updateResources.addResource(webSocketApi)
            val webSocketApiOutput = WebSocketApiOutput(webSocketApi, nimbusState)
            updateOutputs.addOutput(webSocketApiOutput)

            val exportInformation = ExportInformation(
                    webSocketApiOutput.getExportName(),
                    "Created WebSocket API. Base URL is ",
                    "\${NIMBUS_WEBSOCKET_API_URL}")

            val exports = nimbusState.exports.getOrPut(function.stage) { mutableListOf()}
            exports.add(exportInformation)

            webSocketApi
        } else {
            updateTemplate.rootWebSocketApi!!
        }

        val webSocketDeployment = if (updateTemplate.webSocketDeployment == null) {
            val webSocketDeployment = WebSocketDeployment(webSocketApi, nimbusState)
            updateTemplate.webSocketDeployment = webSocketDeployment
            val stage = WebSocketStage(webSocketApi, webSocketDeployment, nimbusState)
            updateResources.addResource(webSocketDeployment)
            updateResources.addResource(stage)
            webSocketDeployment
        } else {
            updateTemplate.webSocketDeployment!!
        }

        val integration = WebSocketIntegration(webSocketApi, function, routeKey, nimbusState)
        val route = WebSocketRoute(webSocketApi, integration, routeKey, nimbusState)

        webSocketDeployment.addDependsOn(route)

        updateResources.addResource(integration)
        updateResources.addResource(route)

        val functionPermissionResource = FunctionPermissionResource(function, webSocketApi, nimbusState)

        updateResources.addResource(functionPermissionResource)
    }
}
