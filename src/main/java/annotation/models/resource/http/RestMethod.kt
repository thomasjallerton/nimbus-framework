package annotation.models.resource.http

import annotation.models.persisted.NimbusState
import annotation.models.resource.Resource
import annotation.models.resource.function.FunctionResource
import com.google.gson.JsonArray
import com.google.gson.JsonObject

class RestMethod (
        private val parent: AbstractRestResource,
        private val method: String,
        private val requestParams: Map<String, String>,
        private val function: FunctionResource,
        nimbusState: NimbusState
): Resource(nimbusState){

    override fun getName(): String {
        return "ApiGatewayMethod${parent.getPath()}$method"
    }

    private fun formatRequestParams(): JsonObject {
        val params = JsonObject()

        for (entry in requestParams.entries) {
            params.addProperty(entry.key, entry.value)
        }

        return params
    }

    override fun toCloudFormation(): JsonObject {
        val methodObj = JsonObject()
        methodObj.addProperty("Type", "AWS::ApiGateway::Method")

        val properties = JsonObject()
        properties.addProperty("HttpMethod", method)
        properties.add("RequestParameters", formatRequestParams())
        properties.add("ResourceId", parent.getId())
        properties.add("RestApiId", parent.getRootId())
        properties.addProperty("ApiKeyRequired", false)
        properties.addProperty("AuthorizationType", "NONE")

        val integration = JsonObject()
        integration.addProperty("IntegrationHttpMethod", "POST")
        integration.addProperty("Type", "AWS_PROXY")
        integration.add("Uri", getFunctionUri())
        properties.add("Integration", integration)

        val methodResponses = JsonArray()
        properties.add("MethodResponses", methodResponses)

        methodObj.add("Properties", properties)

        return methodObj
    }

    private fun getFunctionUri(): JsonObject {
        val uri = JsonObject()
        val joinFunc = JsonArray()
        joinFunc.add("")

        val join = JsonArray()
        join.add("arn:")

        val partitionRef = JsonObject()
        partitionRef.addProperty("Ref", "AWS::Partition")
        join.add(partitionRef)

        join.add(":apigateway:")

        val regionRef = JsonObject()
        regionRef.addProperty("Ref", "AWS::Region")
        join.add(regionRef)

        join.add(":lambda:path/2015-03-31/functions/")

        join.add(function.getArn(""))

        join.add("/invocations")

        joinFunc.add(join)

        uri.add("Fn::Join", joinFunc)
        return uri
    }
}