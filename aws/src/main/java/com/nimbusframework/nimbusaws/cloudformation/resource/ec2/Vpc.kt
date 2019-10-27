package com.nimbusframework.nimbusaws.cloudformation.resource.ec2

import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbusaws.cloudformation.resource.Resource
import com.google.gson.JsonObject

class Vpc(nimbusState: NimbusState, stage: String): Resource(nimbusState, stage) {
    override fun toCloudFormation(): JsonObject {
        val vpc = JsonObject()
        vpc.addProperty("Type", "AWS::EC2::VPC")

        val properties = getProperties()
        properties.addProperty("CidrBlock", "10.0.0.0/16") //TODO: Make this customisable! - perhaps in the properties file
        properties.addProperty("EnableDnsHostnames", true)
        properties.addProperty("EnableDnsSupport", true)

        vpc.add("Properties", properties)
        return vpc
    }

    override fun getName(): String {
        return "${nimbusState.projectName}${stage}VPC"
    }
}