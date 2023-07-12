package com.nimbusframework.nimbusaws.cloudformation.model.resource.http

import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbusaws.cloudformation.model.resource.Resource
import com.google.gson.JsonObject
import java.util.*

class ApiGatewayHttpStage(
    private val httpApi: HttpApi,
    nimbusState: NimbusState
): Resource(nimbusState, httpApi.stage) {

    override fun toCloudFormation(): JsonObject {
        val httpStage = JsonObject()
        httpStage.addProperty("Type", "AWS::ApiGatewayV2::Stage")
        val properties = getProperties()
        properties.add("ApiId", httpApi.getRef())
        properties.addProperty("AutoDeploy", true)
        properties.addProperty("StageName", httpApi.stage)
        httpStage.add("Properties", properties)
        httpStage.add("DependsOn", dependsOn)
        return httpStage
    }

    override fun getName(): String {
        return "${nimbusState.projectName}HttpApiStage"
    }
}
