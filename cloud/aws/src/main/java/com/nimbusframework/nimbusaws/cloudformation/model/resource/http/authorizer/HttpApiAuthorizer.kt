package com.nimbusframework.nimbusaws.cloudformation.model.resource.http.authorizer

import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.nimbusframework.nimbusaws.cloudformation.model.resource.Resource
import com.nimbusframework.nimbusaws.cloudformation.model.resource.http.HttpApi

abstract class HttpApiAuthorizer(
    protected val httpApi: HttpApi,
    val ttlSeconds: Int = 300,
    nimbusState: NimbusState,
    stage: String
) : Resource(nimbusState, stage) {

    private val name = "ApiGatewayHttpApiAuthorizer"

    fun getId(): JsonObject {
        val id = JsonObject()
        val getAttr = JsonArray()
        getAttr.add(name)
        getAttr.add("AuthorizerId")
        id.add("Fn::GetAtt", getAttr)
        return id
    }

    abstract fun addSpecificProperties(properties: JsonObject)

    abstract fun applyToRestMethodProperties(restMethodProperties: JsonObject)

    override fun toCloudFormation(): JsonObject {
        val restApiAuthorizer = JsonObject()
        restApiAuthorizer.addProperty("Type", "AWS::ApiGatewayV2::Authorizer")

        val properties = getProperties()
        properties.addProperty("AuthorizerResultTtlInSeconds", ttlSeconds)
        properties.addProperty("Name", getName())
        properties.add("ApiId", httpApi.getRef())

        addSpecificProperties(properties)

        restApiAuthorizer.add("Properties", properties)

        return restApiAuthorizer
    }

    override fun getName(): String {
        return name
    }

}
