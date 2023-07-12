package com.nimbusframework.nimbusaws.cloudformation.model.resource.http

import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbusaws.cloudformation.model.resource.Resource
import com.google.gson.JsonObject
import java.util.*

class ApiGatewayHttpDeployment(
    private val httpApi: HttpApi,
    nimbusState: NimbusState
): Resource(nimbusState, httpApi.stage) {

    private val creationTime = Calendar.getInstance().timeInMillis

    override fun toCloudFormation(): JsonObject {
        val deployment = JsonObject()
        deployment.addProperty("Type", "AWS::ApiGatewayV2::Deployment")
        val properties = getProperties()
        properties.addProperty("Description", "Nimbus Deployment for project ${nimbusState.projectName}")
        properties.add("ApiId", httpApi.getRef())
        deployment.add("Properties", properties)
        deployment.add("DependsOn", dependsOn)
        return deployment
    }

    override fun getName(): String {
        return "${nimbusState.projectName}HttpApiDeployment$creationTime"
    }
}
