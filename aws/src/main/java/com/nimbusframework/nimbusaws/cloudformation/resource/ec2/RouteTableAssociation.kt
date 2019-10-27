package com.nimbusframework.nimbusaws.cloudformation.resource.ec2

import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbusaws.cloudformation.resource.Resource
import com.google.gson.JsonObject

class RouteTableAssociation(
        private val routeTable: RouteTable,
        private val subnet: Subnet,
        nimbusState: NimbusState
): Resource(nimbusState, routeTable.stage) {

    init {
        addDependsOn(routeTable)
        addDependsOn(subnet)
    }

    override fun toCloudFormation(): JsonObject {
        val routeTableAssociation = JsonObject()
        routeTableAssociation.addProperty("Type", "AWS::EC2::SubnetRouteTableAssociation")

        val properties = getProperties()
        properties.add("RouteTableId", routeTable.getRef())
        properties.add("SubnetId", subnet.getRef())

        routeTableAssociation.add("Properties", properties)

        routeTableAssociation.add("DependsOn", dependsOn)

        return routeTableAssociation
    }

    override fun getName(): String {
        return "Association${routeTable.getName()}${subnet.getName()}"
    }
}