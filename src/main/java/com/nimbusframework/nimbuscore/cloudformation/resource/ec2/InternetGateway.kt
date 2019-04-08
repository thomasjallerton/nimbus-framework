package com.nimbusframework.nimbuscore.cloudformation.resource.ec2

import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbuscore.cloudformation.resource.Resource
import com.google.gson.JsonObject

class InternetGateway(nimbusState: NimbusState, stage: String): Resource(nimbusState, stage) {
    override fun toCloudFormation(): JsonObject {
        val internetGateway = JsonObject()
        internetGateway.addProperty("Type", "AWS::EC2::InternetGateway")

        val properties = getProperties()
        internetGateway.add("Properties", properties)

        return internetGateway
    }

    override fun getName(): String {
        return "InternetGateway"
    }
}