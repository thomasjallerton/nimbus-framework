package cloudformation.resource.ec2

import persisted.NimbusState
import cloudformation.resource.Resource
import com.google.gson.JsonObject

class Route(
        private val routeTable: RouteTable,
        private val internetGateway: InternetGateway,
        nimbusState: NimbusState
): Resource(nimbusState) {

    init {
        addDependsOn(routeTable)
        addDependsOn(internetGateway)
    }

    override fun toCloudFormation(): JsonObject {
        val route = JsonObject()
        route.addProperty("Type", "AWS::EC2::Route")

        val properties = getProperties()
        properties.addProperty("DestinationCidrBlock", "0.0.0.0/0")
        properties.add("RouteTableId", routeTable.getRef())
        properties.add("GatewayId", internetGateway.getRef())

        route.add("Properties", properties)

        route.add("DependsOn", dependsOn)

        return route
    }

    override fun getName(): String {
        return routeTable.getName() + "To" + internetGateway.getName()
    }
}