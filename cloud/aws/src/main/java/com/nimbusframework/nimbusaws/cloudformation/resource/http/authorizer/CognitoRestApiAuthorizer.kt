package com.nimbusframework.nimbusaws.cloudformation.resource.http.authorizer

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.nimbusframework.nimbusaws.cloudformation.resource.http.RestApi
import com.nimbusframework.nimbuscore.persisted.NimbusState

class CognitoRestApiAuthorizer(
    private val cognitoUserPoolArn: String,
    private val identityHeader: String,
    restApi: RestApi,
    ttlSeconds: Int = 300,
    nimbusState: NimbusState,
    stage: String
) : RestApiAuthorizer(restApi, ttlSeconds, nimbusState, stage) {

    override fun addSpecificProperties(properties: JsonObject) {
        properties.addProperty("Type", "COGNITO_USER_POOLS")
        val cognitoArns = JsonArray()
        cognitoArns.add(cognitoUserPoolArn)
        properties.add("ProviderARNs", cognitoArns)
        properties.addProperty("IdentitySource", "method.request.header.$identityHeader")
    }

    override fun applyToRestMethodProperties(restMethodProperties: JsonObject) {
        restMethodProperties.addProperty("AuthorizationType", "COGNITO_USER_POOLS")
        restMethodProperties.add("AuthorizerId", getId())
    }

}
