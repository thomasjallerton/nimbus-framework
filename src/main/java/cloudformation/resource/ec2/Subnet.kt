package cloudformation.resource.ec2

import persisted.NimbusState
import cloudformation.resource.Resource
import com.google.gson.JsonArray
import com.google.gson.JsonObject

class Subnet(
        private val vpc: Vpc,
        private val availabilityZone: String,
        private val cidr: String,
        nimbusState: NimbusState
): Resource(nimbusState, vpc.stage) {

    init {
        addDependsOn(vpc)
    }

    override fun toCloudFormation(): JsonObject {
        val subnet = JsonObject()
        subnet.addProperty("Type", "AWS::EC2::Subnet")

        val properties = getProperties()
        properties.addProperty("CidrBlock", cidr)
        properties.addProperty("MapPublicIpOnLaunch", true)
        properties.add("VpcId", vpc.getRef())

        val joinValues = JsonArray()
        joinValues.add(getRefProperty("AWS::Region"))
        joinValues.add(availabilityZone)

        properties.add("AvailabilityZone", joinJson("", joinValues))


        subnet.add("Properties", properties)
        subnet.add("DependsOn", dependsOn)

        return subnet
    }

    override fun getName(): String {
        return "PubliclyAccessibleSubnet${availabilityZone.toUpperCase()}"
    }

}