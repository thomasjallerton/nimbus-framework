package com.nimbusframework.nimbusaws.cloudformation.model.resource.database

import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbusaws.cloudformation.model.resource.Resource
import com.nimbusframework.nimbusaws.cloudformation.model.resource.ec2.Subnet
import com.google.gson.JsonArray
import com.google.gson.JsonObject

class SubnetGroup(
    private val subnets: List<Subnet>,
    nimbusState: NimbusState,
    stage: String
): Resource(nimbusState, stage) {

    init {
        subnets.forEach{subnet -> addDependsOn(subnet)}
    }
    override fun toCloudFormation(): JsonObject {
        val subnetGroup = JsonObject()
        subnetGroup.addProperty("Type", "AWS::RDS::DBSubnetGroup")

        val properties = getProperties()
        properties.addProperty("DBSubnetGroupDescription", "Publicly available database subnet - created by nimbus")

        val subnetArray = JsonArray()
        subnets.forEach {subnet -> subnetArray.add(subnet.getRef()) }

        properties.add("SubnetIds", subnetArray)

        subnetGroup.add("Properties", properties)
        subnetGroup.add("DependsOn", dependsOn)
        return subnetGroup
    }

    override fun getName(): String {
        return "PublicSubnetGroup"
    }
}
