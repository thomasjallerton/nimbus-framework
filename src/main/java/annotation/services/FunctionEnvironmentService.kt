package annotation.services

import annotation.annotations.function.HttpServerlessFunction
import annotation.annotations.function.NotificationServerlessFunction
import annotation.annotations.function.QueueServerlessFunction
import annotation.cloudformation.outputs.BucketNameOutput
import annotation.cloudformation.outputs.OutputCollection
import annotation.cloudformation.persisted.NimbusState
import annotation.cloudformation.processing.MethodInformation
import annotation.cloudformation.resource.*
import annotation.cloudformation.resource.dynamo.DynamoStreamResource
import annotation.cloudformation.resource.function.FunctionConfig
import annotation.cloudformation.resource.function.FunctionEventMappingResource
import annotation.cloudformation.resource.function.FunctionPermissionResource
import annotation.cloudformation.resource.function.FunctionResource
import annotation.cloudformation.resource.http.*
import annotation.cloudformation.resource.notification.SnsTopicResource
import annotation.cloudformation.resource.queue.QueueResource
import com.google.gson.JsonObject

class FunctionEnvironmentService(
        private val createResources: ResourceCollection,
        private val updateResources: ResourceCollection,
        private val createOutputs: OutputCollection,
        private val updateOutputs: OutputCollection,
        private val nimbusState: NimbusState
) {

    private val restApi: RestApi = RestApi(nimbusState)
    private val apiGatewayDeployment: ApiGatewayDeployment = ApiGatewayDeployment(restApi, nimbusState)

    fun newFunction(handler: String, methodInformation: MethodInformation, functionConfig: FunctionConfig): FunctionResource {
        val function = FunctionResource(handler, methodInformation, functionConfig, nimbusState)
        val logGroup = LogGroupResource(methodInformation.className, methodInformation.methodName, nimbusState)
        val bucket = NimbusBucketResource(nimbusState)

        val iamRoleResource = IamRoleResource(function.getName(), nimbusState)
        iamRoleResource.addAllowStatement("logs:CreateLogStream", logGroup, ":*")
        iamRoleResource.addAllowStatement("logs:PutLogEvents", logGroup, ":*:*")

        function.setIamRoleResource(iamRoleResource)

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

        updateResources.addResource(restApi)
        updateResources.addResource(apiGatewayDeployment)

        var resource: AbstractRestResource = restApi
        updateResources.addResource(resource)

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

        val snsTopic = SnsTopicResource(notificationFunction.topic, function, nimbusState)
        updateResources.addResource(snsTopic)

        val permission = FunctionPermissionResource(function, snsTopic, nimbusState)
        updateResources.addResource(permission)
    }

    fun newQueue(queueFunction: QueueServerlessFunction, function: FunctionResource): QueueResource {

        val sqsQueue = QueueResource(nimbusState, queueFunction.id, queueFunction.timeout * 6)
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
        iamRoleResource.addAllowStatement( "sqs:GetQueueAttributes", sqsQueue, "")

        return sqsQueue
    }

    fun newStoreTrigger(store: Resource, function: FunctionResource) {

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
}