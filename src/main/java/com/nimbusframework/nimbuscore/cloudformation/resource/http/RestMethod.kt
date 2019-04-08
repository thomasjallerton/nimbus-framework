package com.nimbusframework.nimbuscore.cloudformation.resource.http

import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbuscore.cloudformation.resource.Resource
import com.nimbusframework.nimbuscore.cloudformation.resource.function.FunctionResource
import com.google.gson.JsonArray
import com.google.gson.JsonObject

class RestMethod (
        private val parent: AbstractRestResource,
        private val method: String,
        private val requestParams: Map<String, String>,
        private val function: FunctionResource,
        nimbusState: NimbusState
): Resource(nimbusState, function.stage){

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

        val properties = getProperties()
        properties.addProperty("HttpMethod", method)
        properties.add("RequestParameters", formatRequestParams())
        properties.add("ResourceId", parent.getId())
        properties.add("RestApiId", parent.getRootId())
        properties.addProperty("ApiKeyRequired", false)
        properties.addProperty("AuthorizationType", "NONE")

        val integration = JsonObject()
        integration.addProperty("IntegrationHttpMethod", "POST")
        integration.addProperty("Type", "AWS_PROXY")
        integration.add("Uri", function.getUri())
        properties.add("Integration", integration)

        val methodResponses = JsonArray()
        properties.add("MethodResponses", methodResponses)

        methodObj.add("Properties", properties)

        return methodObj
    }

}