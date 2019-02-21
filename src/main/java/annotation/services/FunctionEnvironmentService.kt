package annotation.services

import annotation.annotations.function.HttpServerlessFunction
import annotation.annotations.function.NotificationServerlessFunction
import annotation.annotations.function.QueueServerlessFunction
import annotation.models.outputs.BucketNameOutput
import annotation.models.outputs.OutputCollection
import annotation.models.persisted.NimbusState
import annotation.models.processing.MethodInformation
import annotation.models.resource.*
import annotation.models.resource.dynamo.DynamoStreamResource
import annotation.models.resource.function.FunctionConfig
import annotation.models.resource.function.FunctionEventMappingResource
import annotation.models.resource.function.FunctionPermissionResource
import annotation.models.resource.function.FunctionResource
import annotation.models.resource.http.*
import annotation.models.resource.notification.SnsTopicResource
import annotation.models.resource.queue.QueueResource
import com.google.gson.JsonObject

class FunctionEnvironmentService(
        private val lambdaPolicy: Policy,
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

        lambdaPolicy.addAllowStatement("logs:CreateLogStream", logGroup, ":*")
        lambdaPolicy.addAllowStatement("logs:PutLogEvents", logGroup, ":*:*")

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

        val method = httpFunction.method.toUpperCase()
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

        lambdaPolicy.addAllowStatement("sqs:ReceiveMessage", sqsQueue, "")
        lambdaPolicy.addAllowStatement("sqs:DeleteMessage", sqsQueue, "")
        lambdaPolicy.addAllowStatement( "sqs:GetQueueAttributes", sqsQueue, "")

        return sqsQueue
    }

    fun newDocumentStoreTrigger(documentStore: Resource, function: FunctionResource) {

        val eventMapping = FunctionEventMappingResource(
                documentStore.getAttribute("StreamArn"),
                documentStore.getName(),
                1,
                function,
                nimbusState
        )

        updateResources.addResource(eventMapping)

        val streamSpecification = JsonObject()
        streamSpecification.addProperty("StreamViewType", "NEW_AND_OLD_IMAGES")
        documentStore.addExtraProperty("StreamSpecification", streamSpecification)

        val dynamoStreamResource = DynamoStreamResource(documentStore, nimbusState)

        lambdaPolicy.addAllowStatement("dynamodb:*", dynamoStreamResource, "")
    }
}