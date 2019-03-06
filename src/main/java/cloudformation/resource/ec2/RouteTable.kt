package cloudformation.resource.ec2

import persisted.NimbusState
import cloudformation.resource.Resource
import com.google.gson.JsonObject

class RouteTable(
        private val vpc: Vpc,
        nimbusState: NimbusState
) : Resource(nimbusState) {

    init {
        addDependsOn(vpc)
    }

    override fun toCloudFormation(): JsonObject {
        val routeTable = JsonObject()
        routeTable.addProperty("Type", "AWS::EC2::RouteTable")

        val properties = getProperties()
        properties.add("VpcId", vpc.getRef())

        routeTable.add("Properties", properties)

        routeTable.add("DependsOn", dependsOn)

        return routeTable
    }

    override fun getName(): String {
        return "RoutingTable${vpc.getName()}"
    }
}