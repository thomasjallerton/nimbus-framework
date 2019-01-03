package annotation.services

import annotation.annotations.HttpServerlessFunction
import annotation.models.outputs.BucketNameOutput
import annotation.models.outputs.OutputCollection
import annotation.models.persisted.NimbusState
import annotation.models.resource.*

class FunctionParserService(
        private val lambdaPolicy: Policy,
        private val createResources: ResourceCollection,
        private val updateResources: ResourceCollection,
        private val createOutputs: OutputCollection,
        private val updateOutputs: OutputCollection,
        private val nimbusState: NimbusState
) {

    fun newFunction(handler: String, className: String, methodName: String): FunctionResource {
        val function = FunctionResource(handler, className, methodName, nimbusState)
        val logGroup = LogGroupResource(className, methodName, nimbusState)
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
}