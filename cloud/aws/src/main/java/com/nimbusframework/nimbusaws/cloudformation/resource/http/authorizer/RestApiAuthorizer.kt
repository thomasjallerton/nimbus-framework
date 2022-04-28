package com.nimbusframework.nimbusaws.cloudformation.resource.http.authorizer

import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.nimbusframework.nimbusaws.cloudformation.resource.Resource
import com.nimbusframework.nimbusaws.cloudformation.resource.http.RestApi
import com.nimbusframework.nimbusaws.cloudformation.resource.http.RestMethod

abstract class RestApiAuthorizer(
    private val restApi: RestApi,
    private val ttlSeconds: Int = 300,
    nimbusState: NimbusState,
    stage: String
) : Resource(nimbusState, stage) {

    private val name = "ApiGatewayRestApiAuthorizer"

    fun getId(): JsonObject {
        val parentId = JsonObject()
        val getAttr = JsonArray()
        getAttr.add(name)
        getAttr.add("AuthorizerId")
        parentId.add("Fn::GetAtt", getAttr)
        return parentId
    }

    abstract fun addSpecificProperties(properties: JsonObject)

    abstract fun applyToRestMethodProperties(restMethodProperties: JsonObject)

    override fun toCloudFormation(): JsonObject {
        val restApiAuthorizer = JsonObject()
        restApiAuthorizer.addProperty("Type", "AWS::ApiGateway::Authorizer")

        val properties = getProperties()
        properties.addProperty("AuthorizerResultTtlInSeconds", ttlSeconds)
        properties.addProperty("Name", getName())
        properties.add("RestApiId", restApi.getId())

        addSpecificProperties(properties)



        restApiAuthorizer.add("Properties", properties)

        return restApiAuthorizer
    }

    override fun getName(): String {
        return name
    }

}
