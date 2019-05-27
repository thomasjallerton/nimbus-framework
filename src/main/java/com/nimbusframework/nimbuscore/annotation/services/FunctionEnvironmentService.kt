package com.nimbusframework.nimbuscore.annotation.services

import com.nimbusframework.nimbuscore.annotation.annotations.file.FileStorageEventType
import com.nimbusframework.nimbuscore.annotation.annotations.function.HttpServerlessFunction
import com.nimbusframework.nimbuscore.annotation.annotations.function.NotificationServerlessFunction
import com.nimbusframework.nimbuscore.annotation.annotations.function.QueueServerlessFunction
import com.nimbusframework.nimbuscore.cloudformation.CloudFormationDocuments
import com.nimbusframework.nimbuscore.cloudformation.outputs.BucketNameOutput
import com.nimbusframework.nimbuscore.cloudformation.outputs.RestApiOutput
import com.nimbusframework.nimbuscore.cloudformation.outputs.WebSocketApiOutput
import com.nimbusframework.nimbuscore.cloudformation.processing.MethodInformation
import com.nimbusframework.nimbuscore.cloudformation.resource.IamRoleResource
import com.nimbusframework.nimbuscore.cloudformation.resource.LogGroupResource
import com.nimbusframework.nimbuscore.cloudformation.resource.NimbusBucketResource
import com.nimbusframework.nimbuscore.cloudformation.resource.Resource
import com.nimbusframework.nimbuscore.cloudformation.resource.basic.CronRule
import com.nimbusframework.nimbuscore.cloudformation.resource.dynamo.DynamoStreamResource
import com.nimbusframework.nimbuscore.cloudformation.resource.file.FileBucket
import com.nimbusframework.nimbuscore.cloudformation.resource.file.LambdaConfiguration
import com.nimbusframework.nimbuscore.cloudformation.resource.function.FunctionConfig
import com.nimbusframework.nimbuscore.cloudformation.resource.function.FunctionEventMappingResource
import com.nimbusframework.nimbuscore.cloudformation.resource.function.FunctionPermissionResource
import com.nimbusframework.nimbuscore.cloudformation.resource.function.FunctionResource
import com.nimbusframework.nimbuscore.cloudformation.resource.http.*
import com.nimbusframework.nimbuscore.cloudformation.resource.notification.SnsTopicResource
import com.nimbusframework.nimbuscore.cloudformation.resource.queue.QueueResource
import com.nimbusframework.nimbuscore.cloudformation.resource.websocket.*
import com.google.gson.JsonObject
import com.nimbusframework.nimbuscore.annotation.annotations.function.HttpMethod
import com.nimbusframework.nimbuscore.persisted.ExportInformation
import com.nimbusframework.nimbuscore.persisted.HandlerInformation
import com.nimbusframework.nimbuscore.persisted.NimbusState

class FunctionEnvironmentService(
        private val cloudFormationDocumentsCollection: MutableMap<String, CloudFormationDocuments>,
        private val nimbusState: NimbusState
) {


    fun newFunction(handler: String, methodInformation: MethodInformation, handlerInformation: HandlerInformation, functionConfig: FunctionConfig): FunctionResource {
        nimbusState.handlerFiles.add(handlerInformation)

        val function = FunctionResource(handler, methodInformation, functionConfig, handlerInformation, nimbusState)
        val logGroup = LogGroupResource(methodInformation.className, methodInformation.methodName, function, nimbusState, functionConfig.stage)
        val bucket = NimbusBucketResource(nimbusState, functionConfig.stage)

        val iamRoleResource = IamRoleResource(function.getShortName(), nimbusState, functionConfig.stage)
        iamRoleResource.addAllowStatement("logs:CreateLogStream", logGroup, ":*")
        iamRoleResource.addAllowStatement("logs:PutLogEvents", logGroup, ":*:*")

        function.setIamRoleResource(iamRoleResource)
        function.addEnvVariable("NIMBUS_STAGE", functionConfig.stage)

        val cloudFormationDocuments = cloudFormationDocumentsCollection.getOrPut(functionConfig.stage) { CloudFormationDocuments() }
        val updateResources = cloudFormationDocuments.updateResources
        val createResources = cloudFormationDocuments.createResources
        val createOutputs = cloudFormationDocuments.createOutputs
        val updateOutputs = cloudFormationDocuments.updateOutputs

        updateResources.addResource(iamRoleResource)
        updateResources.addResource(function)
        updateResources.addResource(logGroup)
        updateResources.addResource(bucket)

        createResources.addResource(bucket)

        val bucketName = BucketNameOutput(bucket, nimbusState)
        createOutputs.addOutput(bucketName)
        updateOutputs.addOutput(bucketName)

        return function
    }

    fun newHttpMethod(httpFunction: HttpServerlessFunction, function: FunctionResource) {
        val pathParts = httpFunction.path.split("/")

        val cfDocuments = cloudFormationDocumentsCollection[function.stage]!!
        val updateResources = cfDocuments.updateResources
        val updateOutputs = cfDocuments.updateOutputs

        val restApi = if (cfDocuments.rootRestApi == null) {
            val restApi = RestApi(nimbusState, function.stage)
            cfDocuments.rootRestApi = restApi
            updateResources.addResource(restApi)
            val httpApiOutput = RestApiOutput(restApi, nimbusState)
            updateOutputs.addOutput(httpApiOutput)

            val exportInformation = ExportInformation(
                    httpApiOutput.getExportName(),
                    "Created REST API. Base URL is ",
                    "\${NIMBUS_REST_API_URL}")

            val exports = nimbusState.exports.getOrPut(function.stage) { mutableListOf()}
            exports.add(exportInformation)

            restApi
        } else {
            cfDocuments.rootRestApi!!
        }

        val apiGatewayDeployment = if (cfDocuments.apiGatewayDeployment == null) {
            val apiGatewayDeployment = ApiGatewayDeployment(restApi, nimbusState)
            cfDocuments.apiGatewayDeployment = apiGatewayDeployment
            updateResources.addResource(apiGatewayDeployment)
            apiGatewayDeployment
        } else {
            cfDocuments.apiGatewayDeployment!!
        }

        var resource: AbstractRestResource = restApi

        for (part in pathParts) {
            if (part.isNotEmpty()) {
                resource = RestApiResource(resource, part, nimbusState)
                updateResources.addResource(resource)
            }
        }

        val method = httpFunction.method.name
        val restMethod = RestMethod(resource, method, mapOf(), function, nimbusState)
        apiGatewayDeployment.addDependsOn(restMethod)
        updateResources.addResource(restMethod)

        if (httpFunction.method != HttpMethod.OPTIONS && httpFunction.method != HttpMethod.ANY) {
            if (httpFunction.allowedCorsOrigin.isNotEmpty()) {

                val newCorsMethod = CorsRestMethod(
                        resource,
                        cfDocuments,
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
                corsMethod.addHeaders(httpFunction.allowedCorsHeaders)
                corsMethod.addOrigin(httpFunction.allowedCorsOrigin)
                corsMethod.addMethod(httpFunction.method)
            }
        }

        val permission = FunctionPermissionResource(function, restApi, nimbusState)
        updateResources.addResource(permission)

    }

    fun newNotification(notificationFunction: NotificationServerlessFunction, function: FunctionResource) {
        val cfDocuments = cloudFormationDocumentsCollection[function.stage]!!
        val updateResources = cfDocuments.updateResources

        val snsTopic = SnsTopicResource(notificationFunction.topic, function, nimbusState, function.stage)
        updateResources.addResource(snsTopic)

        val permission = FunctionPermissionResource(function, snsTopic, nimbusState)
        updateResources.addResource(permission)
    }

    fun newQueue(queueFunction: QueueServerlessFunction, function: FunctionResource) {
        val cfDocuments = cloudFormationDocumentsCollection[function.stage]!!
        val updateResources = cfDocuments.updateResources

        val sqsQueue = QueueResource(nimbusState, queueFunction.id, queueFunction.timeout * 6, function.stage)
        updateResources.addResource(sqsQueue)

        val eventMapping = FunctionEventMappingResource(
                sqsQueue.getArn(""),
                sqsQueue.getName(),
                queueFunction.batchSize,
                function,
                false,
                nimbusState
        )
        updateResources.addResource(eventMapping)

        val iamRoleResource = function.getIamRoleResource()

        iamRoleResource.addAllowStatement("sqs:ReceiveMessage", sqsQueue, "")
        iamRoleResource.addAllowStatement("sqs:DeleteMessage", sqsQueue, "")
        iamRoleResource.addAllowStatement("sqs:GetQueueAttributes", sqsQueue, "")
    }

    fun newStoreTrigger(store: Resource, function: FunctionResource) {
        val cfDocuments = cloudFormationDocumentsCollection[function.stage]!!
        val updateResources = cfDocuments.updateResources

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

        function.getIamRoleResource().addAllowStatement("dynamodb:*", dynamoStreamResource, "")
    }

    fun newCronTrigger(cron: String, function: FunctionResource) {
        val cfDocuments = cloudFormationDocumentsCollection[function.stage]!!
        val updateResources = cfDocuments.updateResources

        val cronRule = CronRule(cron, function, nimbusState)
        val lambdaPermissionResource = FunctionPermissionResource(function, cronRule, nimbusState)

        updateResources.addResource(cronRule)
        updateResources.addResource(lambdaPermissionResource)
    }

    fun newFileTrigger(name: String, eventType: FileStorageEventType, function: FunctionResource) {
        val newBucket = FileBucket(nimbusState, name, arrayOf(), function.stage)

        val updateResources = cloudFormationDocumentsCollection[function.stage]!!.updateResources
        val oldBucket = updateResources.get(newBucket.getName()) as FileBucket?

        val lambdaConfiguration = LambdaConfiguration(eventType, function)

        if (oldBucket != null) {
            val permission = FunctionPermissionResource(function, oldBucket, nimbusState)
            oldBucket.addDependsOn(function)
            oldBucket.addDependsOn(permission)
            updateResources.addResource(permission)
            oldBucket.addLambdaConfiguration(lambdaConfiguration)
        } else {
            newBucket.addLambdaConfiguration(lambdaConfiguration)
            val permission = FunctionPermissionResource(function, newBucket, nimbusState)
            newBucket.addDependsOn(function)
            newBucket.addDependsOn(permission)
            updateResources.addResource(permission)
            updateResources.addResource(newBucket)
        }
    }

    fun newWebSocketRoute(routeKey: String, function: FunctionResource) {
        val cfDocuments = cloudFormationDocumentsCollection[function.stage]!!
        val updateResources = cfDocuments.updateResources
        val updateOutputs = cfDocuments.updateOutputs

        val webSocketApi = if (cfDocuments.rootWebSocketApi == null) {
            val webSocketApi = WebSocketApi(nimbusState, function.stage)
            cfDocuments.rootWebSocketApi = webSocketApi
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
            cfDocuments.rootWebSocketApi!!
        }

        val webSocketDeployment = if (cfDocuments.webSocketDeployment == null) {
            val webSocketDeployment = WebSocketDeployment(webSocketApi, nimbusState)
            cfDocuments.webSocketDeployment = webSocketDeployment
            val stage = WebSocketStage(webSocketApi, webSocketDeployment, nimbusState)
            updateResources.addResource(webSocketDeployment)
            updateResources.addResource(stage)
            webSocketDeployment
        } else {
            cfDocuments.webSocketDeployment!!
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