package com.nimbusframework.nimbusaws.cloudformation.model.resource.http

import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbusaws.cloudformation.model.resource.Resource
import com.nimbusframework.nimbusaws.cloudformation.model.resource.function.FunctionResource
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.nimbusframework.nimbusaws.cloudformation.model.resource.function.FunctionTrigger
import com.nimbusframework.nimbusaws.cloudformation.model.resource.http.authorizer.HttpApiAuthorizer

class RestRoute (
    private val parentApi: HttpApi,
    initialPath: String,
    private val method: String,
    private val httpLambdaIntegration: HttpLambdaIntegration,
    private val authorizer: HttpApiAuthorizer?,
    nimbusState: NimbusState
): Resource(nimbusState, httpLambdaIntegration.stage), FunctionTrigger {

    private val path = if (initialPath.startsWith("/")) initialPath else "/$initialPath"

    init {
        if (authorizer != null) {
            addDependsOn(authorizer)
        }
    }

    override fun getName(): String {
        return "ApiGatewayRoute${path.replace(Regex("[/{}_-]"), "")}$method"
    }

    override fun getTriggerArn(): JsonObject {
        return parentApi.getArn("/*/*$path")
    }

    override fun getTriggerType(): String {
        return "apigateway."
    }

    override fun getTriggerName(): String {
        return "HttpApi"
    }

    override fun toCloudFormation(): JsonObject {
        val methodObj = JsonObject()
        methodObj.addProperty("Type", "AWS::ApiGatewayV2::Route")

        val properties = getProperties()
        val routeKey = "$method $path"
        properties.add("ApiId", parentApi.getRef())
        properties.addProperty("RouteKey", routeKey)
        val integrationArray = JsonArray()
        integrationArray.add("integrations")
        integrationArray.add(httpLambdaIntegration.getRef())
        properties.add("Target", joinJson("/", integrationArray))

        if (authorizer != null) {
            authorizer.applyToRestMethodProperties(properties)
        } else {
            properties.addProperty("AuthorizationType", "NONE")
        }

        methodObj.add("Properties", properties)

        return methodObj
    }

}
