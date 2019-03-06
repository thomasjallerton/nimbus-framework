package cloudformation.resource.ec2

import persisted.NimbusState
import cloudformation.resource.Resource
import com.google.gson.JsonObject

class InternetGateway(nimbusState: NimbusState): Resource(nimbusState) {
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