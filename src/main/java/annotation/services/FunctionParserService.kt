package annotation.services

import annotation.annotations.HttpServerlessFunction
import annotation.annotations.NotificationServerlessFunction
import annotation.models.outputs.BucketNameOutput
import annotation.models.outputs.OutputCollection
import annotation.models.persisted.NimbusState
import annotation.models.processing.MethodInformation
import annotation.models.resource.*

class FunctionParserService(
        private val lambdaPolicy: Policy,
        private val createResources: ResourceCollection,
        private val updateResources: ResourceCollection,
        private val createOutputs: OutputCollection,
        private val updateOutputs: OutputCollection,
        private val nimbusState: NimbusState
) {

    fun newFunction(handler: String, methodInformation: MethodInformation): FunctionResource {
        val function = FunctionResource(handler, methodInformation.className, methodInformation.methodName, nimbusState)
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
}