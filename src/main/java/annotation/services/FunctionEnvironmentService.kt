package annotation.services

import annotation.annotations.NimbusConstants.stage
import annotation.annotations.file.FileStorageEventType
import annotation.annotations.function.HttpServerlessFunction
import annotation.annotations.function.NotificationServerlessFunction
import annotation.annotations.function.QueueServerlessFunction
import cloudformation.CloudFormationDocuments
import cloudformation.outputs.BucketNameOutput
import cloudformation.outputs.RestApiOutput
import persisted.NimbusState
import cloudformation.processing.MethodInformation
import cloudformation.resource.*
import cloudformation.resource.basic.CronRule
import cloudformation.resource.dynamo.DynamoStreamResource
import cloudformation.resource.file.FileBucket
import cloudformation.resource.file.LambdaConfiguration
import cloudformation.resource.function.FunctionConfig
import cloudformation.resource.function.FunctionEventMappingResource
import cloudformation.resource.function.FunctionPermissionResource
import cloudformation.resource.function.FunctionResource
import cloudformation.resource.http.*
import cloudformation.resource.notification.SnsTopicResource
import cloudformation.resource.queue.QueueResource
import com.google.gson.JsonObject

class FunctionEnvironmentService(
        private val cloudFormationDocumentsCollection: MutableMap<String, CloudFormationDocuments>,
        private val nimbusState: NimbusState
) {


    fun newFunction(handler: String, methodInformation: MethodInformation, functionConfig: FunctionConfig): FunctionResource {
        val function = FunctionResource(handler, methodInformation, functionConfig, nimbusState)
        val logGroup = LogGroupResource(methodInformation.className, methodInformation.methodName, function, nimbusState, functionConfig.stage)
        val bucket = NimbusBucketResource(nimbusState, functionConfig.stage)

        val iamRoleResource = IamRoleResource(function.getName(), nimbusState, functionConfig.stage)
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
            updateOutputs.addOutput(RestApiOutput(restApi, nimbusState))
            nimbusState.hasHttpServerlessFunctions = true
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

    fun newQueue(queueFunction: QueueServerlessFunction, function: FunctionResource): QueueResource {
        val cfDocuments = cloudFormationDocumentsCollection[function.stage]!!
        val updateResources = cfDocuments.updateResources

        val sqsQueue = QueueResource(nimbusState, queueFunction.id, queueFunction.timeout * 6, function.stage)
        updateResources.addResource(sqsQueue)

        val eventMapping = FunctionEventMappingResource(
                sqsQueue.getArn(""),
                sqsQueue.getName(),
                queueFunction.batchSize,
                function,
                nimbusState
        )
        updateResources.addResource(eventMapping)

        val iamRoleResource = function.getIamRoleResource()

        iamRoleResource.addAllowStatement("sqs:ReceiveMessage", sqsQueue, "")
        iamRoleResource.addAllowStatement("sqs:DeleteMessage", sqsQueue, "")
        iamRoleResource.addAllowStatement("sqs:GetQueueAttributes", sqsQueue, "")

        return sqsQueue
    }

    fun newStoreTrigger(store: Resource, function: FunctionResource) {
        val cfDocuments = cloudFormationDocumentsCollection[function.stage]!!
        val updateResources = cfDocuments.updateResources

        val eventMapping = FunctionEventMappingResource(
                store.getAttribute("StreamArn"),
                store.getName(),
                1,
                function,
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
        val newBucket = FileBucket(nimbusState, name, function.stage)

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
}