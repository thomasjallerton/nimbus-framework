package cloudformation.resource.database

import persisted.NimbusState
import cloudformation.resource.Resource
import cloudformation.resource.ec2.SecurityGroupResource
import com.google.gson.JsonArray
import com.google.gson.JsonObject

class RdsResource(
        val databaseConfiguration: DatabaseConfiguration,
        private val securityGroup: SecurityGroupResource,
        private val subnetGroup: SubnetGroup,
        nimbusState: NimbusState
): Resource(nimbusState, securityGroup.stage) {

    init {
        addDependsOn(securityGroup)
        addDependsOn(subnetGroup)
    }

    override fun toCloudFormation(): JsonObject {
        val dbInstance = JsonObject()
        dbInstance.addProperty("Type", "AWS::RDS::DBInstance")

        val properties = getProperties()
        properties.addProperty("AllocatedStorage", databaseConfiguration.size)
        properties.addProperty("DBInstanceIdentifier", "${databaseConfiguration.name}$stage")
        properties.addProperty("DBInstanceClass", databaseConfiguration.databaseSize.toInstanceClass())
        properties.addProperty("Engine", databaseConfiguration.databaseLanguage.toEngine(databaseConfiguration.databaseSize))
        properties.addProperty("PubliclyAccessible", true)
        properties.addProperty("MasterUsername", databaseConfiguration.username)
        properties.addProperty("MasterUserPassword", databaseConfiguration.password)
        properties.addProperty("StorageType", "gp2")
        properties.addProperty("AllocatedStorage", databaseConfiguration.size)

        val securityGroups = JsonArray()
        securityGroups.add(securityGroup.getRef())

        properties.add("VPCSecurityGroups", securityGroups)

        properties.add("DBSubnetGroupName", subnetGroup.getRef())

        dbInstance.add("Properties", properties)

        dbInstance.add("DependsOn", dependsOn)

        return dbInstance
    }

    override fun getName(): String {
        return "${databaseConfiguration.name}RdsInstance"
    }
}