package com.nimbusframework.nimbuscore.cloudformation.resource.ec2

import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbuscore.cloudformation.resource.Resource
import com.google.gson.JsonObject

class VpcGatewayAttachment(
        private val vpc: Vpc,
        private val internetGateway: InternetGateway,
        nimbusState: NimbusState
): Resource(nimbusState, vpc.stage) {

    init {
        addDependsOn(vpc)
        addDependsOn(internetGateway)
    }

    override fun toCloudFormation(): JsonObject {
        val internetGatewayAttachment = JsonObject()
        internetGatewayAttachment.addProperty("Type", "AWS::EC2::VPCGatewayAttachment")
        val properties = getProperties()

        properties.add("InternetGatewayId", internetGateway.getRef())
        properties.add("VpcId", vpc.getRef())

        internetGatewayAttachment.add("Properties", properties)
        internetGatewayAttachment.add("DependsOn", dependsOn)

        return internetGatewayAttachment
    }

    override fun getName(): String {
        return vpc.getName() + "InternetGatewayAttachment"
    }

}