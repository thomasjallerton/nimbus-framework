package com.nimbusframework.nimbuscore.cloudformation.resource.http

import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbuscore.cloudformation.resource.Resource
import com.google.gson.JsonObject
import java.util.*

class ApiGatewayDeployment(
        private val restApi: RestApi,
        nimbusState: NimbusState
):Resource(nimbusState, restApi.stage) {

    private val creationTime = Calendar.getInstance().timeInMillis

    override fun toCloudFormation(): JsonObject {
        val deployment = JsonObject()
        deployment.addProperty("Type", "AWS::ApiGateway::Deployment")
        val properties = getProperties()
        properties.addProperty("Description", "Nimbus Deployment for project ${nimbusState.projectName}")
        properties.add("RestApiId", restApi.getRef())
        properties.addProperty("StageName", stage)
        deployment.add("Properties", properties)
        deployment.add("DependsOn", dependsOn)
        return deployment
    }

    override fun getName(): String {
        return "${nimbusState.projectName}ApiGatewayDeployment$creationTime"
    }
}