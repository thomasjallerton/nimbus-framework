package com.nimbusframework.nimbusaws.cloudformation.model.resource.http

import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbusaws.cloudformation.model.resource.Resource
import com.nimbusframework.nimbusaws.cloudformation.model.resource.function.FunctionResource
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.nimbusframework.nimbusaws.cloudformation.model.resource.http.authorizer.HttpApiAuthorizer

class HttpLambdaIntegration (
    private val parentApi: HttpApi,
    private val path: String,
    private val method: String,
    private val function: FunctionResource,
    nimbusState: NimbusState
): Resource(nimbusState, function.stage){

    override fun getName(): String {
        return "ApiGatewayIntegration${path.replace(Regex("[/{}_-]"), "")}$method"
    }

    override fun toCloudFormation(): JsonObject {
        val methodObj = JsonObject()
        methodObj.addProperty("Type", "AWS::ApiGatewayV2::Integration")

        val properties = getProperties()
        properties.add("ApiId", parentApi.getRef())
        properties.addProperty("IntegrationType", "AWS_PROXY")
        properties.addProperty("PayloadFormatVersion", "2.0")
        properties.add("IntegrationUri", function.getUri())

        methodObj.add("Properties", properties)

        return methodObj
    }

}
