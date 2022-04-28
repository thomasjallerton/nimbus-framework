package com.nimbusframework.nimbusaws.cloudformation.resource.http

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.nimbusframework.nimbuscore.annotations.function.HttpMethod
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationTemplate
import com.nimbusframework.nimbusaws.cloudformation.resource.Resource
import com.nimbusframework.nimbuscore.persisted.NimbusState

class CorsRestMethod (
        private val parent: AbstractRestResource,
        private val cloudFormationDocuments: CloudFormationTemplate,
        nimbusState: NimbusState
): Resource(nimbusState, parent.stage){

    private val allowedHeaders: MutableSet<String> = mutableSetOf()
    private val allowedOrigins: MutableSet<String> = mutableSetOf()
    private val allowedMethods: MutableSet<String> = mutableSetOf()

    fun addMethod(method: HttpMethod) {
        allowedMethods.add(method.name)
    }

    fun addHeaders(headers: Array<String>) {
        allowedHeaders.addAll(headers)
    }

    fun addOrigin(origin: String) {
        allowedOrigins.add(origin)
    }

    override fun getName(): String {
        return "ApiGatewayCorsMethod${parent.getPath()}"
    }

    override fun toCloudFormation(): JsonObject {
        val methodObj = JsonObject()
        methodObj.addProperty("Type", "AWS::ApiGateway::Method")

        val properties = getProperties()
        properties.addProperty("HttpMethod", "OPTIONS")
        properties.add("ResourceId", parent.getId())
        properties.add("RestApiId", parent.getRootId())
        properties.addProperty("ApiKeyRequired", false)
        properties.addProperty("AuthorizationType", "NONE")

        val integration = JsonObject()
        integration.addProperty("Type", "MOCK")

        val requestTemplates = JsonObject()
        requestTemplates.addProperty("application/json", "{\"statusCode\": 200}")
        integration.add("RequestTemplates", requestTemplates)

        val integrationResponses = JsonArray()
        val integrationResponse = JsonObject()
        integrationResponse.addProperty("StatusCode", "200")

        val responseTemplates = JsonObject()
        responseTemplates.addProperty("application/json", "")
        integrationResponse.add("ResponseTemplates", responseTemplates)


        val responseParameters = JsonObject()
        val methods = allowedMethods.reduce { acc, item -> "$acc,$item"}
        responseParameters.addProperty("method.response.header.Access-Control-Allow-Methods", "'$methods'")

        var strAllowedHeaders = "origin,content-type"
        this.allowedHeaders.forEach { strAllowedHeaders += ",$it" }
        println(strAllowedHeaders)
        println(allowedHeaders)

        responseParameters.addProperty("method.response.header.Access-Control-Allow-Headers", "'$strAllowedHeaders'")

        val origin = if (allowedOrigins.size == 1) {
            allowedOrigins.first()
        } else {
            "*"
        }

        val referencedBucket = cloudFormationDocuments.referencedFileStorageBucket(origin)
        if (referencedBucket == null) {
            responseParameters.addProperty("method.response.header.Access-Control-Allow-Origin", "'$origin'")
        } else {
            val joinVals = JsonArray()
            joinVals.add("'")
            joinVals.add(referencedBucket.getAttr("WebsiteURL"))
            joinVals.add("'")
            responseParameters.add("method.response.header.Access-Control-Allow-Origin", joinJson("", joinVals))
        }

        integrationResponse.add("ResponseParameters", responseParameters)
        integrationResponses.add(integrationResponse)

        integration.add("IntegrationResponses", integrationResponses)
        properties.add("Integration", integration)

        val methodResponses = JsonArray()
        val methodResponse = JsonObject()
        methodResponse.addProperty("StatusCode", "200")

        val responseParametersMethod = JsonObject()
        responseParametersMethod.addProperty("method.response.header.Access-Control-Allow-Headers", true)
        responseParametersMethod.addProperty("method.response.header.Access-Control-Allow-Methods", true)
        responseParametersMethod.addProperty("method.response.header.Access-Control-Allow-Origin", true)

        methodResponse.add("ResponseParameters", responseParametersMethod)

        val responseModels = JsonObject()
        responseModels.addProperty("application/json", "Empty")
        methodResponse.add("ResponseModels", responseModels)

        methodResponses.add(methodResponse)
        properties.add("MethodResponses", methodResponses)

        methodObj.add("Properties", properties)

        return methodObj
    }
}
