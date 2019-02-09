package annotation.services

import annotation.annotations.function.HttpServerlessFunction
import annotation.annotations.function.NotificationServerlessFunction
import annotation.annotations.function.QueueServerlessFunction
import annotation.models.outputs.BucketNameOutput
import annotation.models.outputs.OutputCollection
import annotation.models.persisted.NimbusState
import annotation.models.processing.MethodInformation
import annotation.models.resource.*
import annotation.models.resource.function.FunctionConfig
import annotation.models.resource.function.FunctionEventMappingResource
import annotation.models.resource.function.FunctionPermissionResource
import annotation.models.resource.function.FunctionResource
import annotation.models.resource.http.AbstractRestResource
import annotation.models.resource.http.RestApi
import annotation.models.resource.http.RestApiResource
import annotation.models.resource.http.RestMethod
import annotation.models.resource.notification.SnsTopicResource
import annotation.models.resource.queue.QueueResource

class FunctionEnvironmentService(
        val lambdaPolicy: Policy,
        private val createResources: ResourceCollection,
        private val updateResources: ResourceCollection,
        private val createOutputs: OutputCollection,
        private val updateOutputs: OutputCollection,
        private val nimbusState: NimbusState
) {

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
        val root = RestApi(nimbusState)
        var resource: AbstractRestResource = root
        updateResources.addResource(resource)
        for (part in pathParts) {
            if (part.isNotEmpty()) {
                resource = RestApiResource(resource, part, nimbusState)
                updateResources.addResource(resource)
            }
        }

        val method = httpFunction.method.toUpperCase()
        val restMethod = RestMethod(resource, method, mapOf(), function, nimbusState)
        updateResources.addResource(restMethod)

        val permission = FunctionPermissionResource(function, root, nimbusState)
        updateResources.addResource(permission)
    }

    fun newNotification(notificationFunction: NotificationServerlessFunction, function: FunctionResource) {

        val snsTopic = SnsTopicResource(notificationFunction.topic, function, nimbusState)
        updateResources.addResource(snsTopic)

        val permission = FunctionPermissionResource(function, snsTopic, nimbusState)
        updateResources.addResource(permission)
    }

    fun newQueue(queueFunction: QueueServerlessFunction, function: FunctionResource): QueueResource {

        val sqsQueue = QueueResource(nimbusState, queueFunction.id)
        updateResources.addResource(sqsQueue)

        val eventMapping = FunctionEventMappingResource(sqsQueue, queueFunction.batchSize, function, nimbusState)
        updateResources.addResource(eventMapping)

        lambdaPolicy.addAllowStatement("sqs:ReceiveMessage", sqsQueue, "")
        lambdaPolicy.addAllowStatement("sqs:DeleteMessage", sqsQueue, "")
        lambdaPolicy.addAllowStatement( "sqs:GetQueueAttributes", sqsQueue, "")

        return sqsQueue
    }
}