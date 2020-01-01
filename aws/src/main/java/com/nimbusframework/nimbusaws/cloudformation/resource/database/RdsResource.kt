package com.nimbusframework.nimbusaws.cloudformation.resource.database

import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbusaws.cloudformation.resource.Resource
import com.nimbusframework.nimbusaws.cloudformation.resource.ec2.SecurityGroupResource
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.nimbusframework.nimbuscore.annotations.database.DatabaseLanguage
import com.nimbusframework.nimbuscore.annotations.database.DatabaseSize

class RdsResource(
        val rdsConfiguration: RdsConfiguration,
        private val securityGroup: SecurityGroupResource,
        private val subnetGroup: SubnetGroup,
        nimbusState: NimbusState
) : Resource(nimbusState, securityGroup.stage) {

    init {
        addDependsOn(securityGroup)
        addDependsOn(subnetGroup)
    }

    override fun toCloudFormation(): JsonObject {
        val dbInstance = JsonObject()
        dbInstance.addProperty("Type", "AWS::RDS::DBInstance")

        val properties = getProperties()
        properties.addProperty("AllocatedStorage", rdsConfiguration.size)
        properties.addProperty("DBInstanceIdentifier", "${rdsConfiguration.name}$stage")
        properties.addProperty("DBInstanceClass", rdsConfiguration.awsDatabaseInstance)
        properties.addProperty("Engine", toEngine(rdsConfiguration.databaseLanguage, rdsConfiguration.awsDatabaseInstance))
        properties.addProperty("PubliclyAccessible", true)
        properties.addProperty("MasterUsername", rdsConfiguration.username)
        properties.addProperty("MasterUserPassword", rdsConfiguration.password)
        properties.addProperty("StorageType", "gp2")
        properties.addProperty("AllocatedStorage", rdsConfiguration.size)

        val securityGroups = JsonArray()
        securityGroups.add(securityGroup.getRef())

        properties.add("VPCSecurityGroups", securityGroups)

        properties.add("DBSubnetGroupName", subnetGroup.getRef())

        dbInstance.add("Properties", properties)

        dbInstance.add("DependsOn", dependsOn)

        return dbInstance
    }

    override fun getName(): String {
        return "RdsInstance${rdsConfiguration.name}"
    }

    private fun toEngine(language: DatabaseLanguage, instanceClass: String): String {
        return if (instanceClass == RdsConfiguration.toInstanceType(DatabaseSize.FREE)) {
            toFreeEngine(language)
        } else {
            toPaidEngine(language)
        }
    }

    private fun toFreeEngine(language: DatabaseLanguage): String {
        return when (language) {
            DatabaseLanguage.MYSQL -> "mysql"
            DatabaseLanguage.ORACLE -> "oracle-ee"
            DatabaseLanguage.MARIADB -> "mariadb"
            DatabaseLanguage.SQLSERVER -> "sqlserver-ex"
            DatabaseLanguage.POSTGRESQL -> "postgres"
        }
    }

    private fun toPaidEngine(language: DatabaseLanguage): String {
        return when (language) {
            DatabaseLanguage.MYSQL -> "aurora-mysql"
            DatabaseLanguage.ORACLE -> "oracle-ee"
            DatabaseLanguage.MARIADB -> "mariadb"
            DatabaseLanguage.SQLSERVER -> "sqlserver-ee"
            DatabaseLanguage.POSTGRESQL -> "aurora-postgresql"
        }
    }
}