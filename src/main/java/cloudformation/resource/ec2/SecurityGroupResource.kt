package cloudformation.resource.ec2

import persisted.NimbusState
import cloudformation.resource.Resource
import com.google.gson.JsonArray
import com.google.gson.JsonObject

class SecurityGroupResource(
        private val vpc: Vpc,
        nimbusState: NimbusState
): Resource(nimbusState) {

    init {
        addDependsOn(vpc)
    }

    override fun toCloudFormation(): JsonObject {
        val securityGroup = JsonObject()
        securityGroup.addProperty("Type", "AWS::EC2::SecurityGroup")

        val properties = getProperties()

        val listOfAuthorised = JsonArray()

        val anyIps = JsonObject()
        anyIps.addProperty("CidrIp", "0.0.0.0/0")
        anyIps.addProperty("IpProtocol", "tcp")
        anyIps.addProperty("FromPort", "3306")
        anyIps.addProperty("ToPort", "3306")

        val anyIps2 = JsonObject()
        anyIps2.addProperty("CidrIpv6", "::/0")
        anyIps2.addProperty("IpProtocol", "tcp")
        anyIps2.addProperty("FromPort", "3306")
        anyIps2.addProperty("ToPort", "3306")

        listOfAuthorised.add(anyIps)
        listOfAuthorised.add(anyIps2)

        properties.add("SecurityGroupIngress", listOfAuthorised)
        properties.add("SecurityGroupEgress", listOfAuthorised)
        properties.addProperty("GroupDescription", "Allows all incoming for databases")
        properties.add("VpcId", vpc.getRef())

        securityGroup.add("DependsOn", dependsOn)

        securityGroup.add("Properties", properties)
        return securityGroup
    }

    override fun getName(): String {
        return "PublicDBSecurityGroup"
    }

}