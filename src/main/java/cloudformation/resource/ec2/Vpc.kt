package cloudformation.resource.ec2

import persisted.NimbusState
import cloudformation.resource.Resource
import com.google.gson.JsonObject

class Vpc(nimbusState: NimbusState): Resource(nimbusState) {
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
        return "${nimbusState.projectName}VPC"
    }
}