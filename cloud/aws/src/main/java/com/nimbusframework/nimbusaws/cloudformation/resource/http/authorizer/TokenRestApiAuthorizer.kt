package com.nimbusframework.nimbusaws.cloudformation.resource.http.authorizer

import com.google.gson.JsonObject
import com.nimbusframework.nimbusaws.cloudformation.resource.function.FunctionResource
import com.nimbusframework.nimbusaws.cloudformation.resource.http.RestApi
import com.nimbusframework.nimbuscore.persisted.NimbusState

class TokenRestApiAuthorizer(
    private val function: FunctionResource,
    private val identityHeader: String,
    restApi: RestApi,
    ttlSeconds: Int = 300,
    nimbusState: NimbusState,
    stage: String
) : RestApiAuthorizer(restApi, ttlSeconds, nimbusState, stage) {

    override fun addSpecificProperties(properties: JsonObject) {
        properties.addProperty("Type", "TOKEN")
        properties.add("AuthorizerUri", function.getUri())
        properties.addProperty("IdentitySource", "method.request.header.$identityHeader")
    }

    override fun applyToRestMethodProperties(restMethodProperties: JsonObject) {
        restMethodProperties.addProperty("AuthorizationType", "CUSTOM")
        restMethodProperties.add("AuthorizerId", getId())
    }

}
