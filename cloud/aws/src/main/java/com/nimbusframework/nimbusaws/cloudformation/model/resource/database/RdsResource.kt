package com.nimbusframework.nimbusaws.cloudformation.model.resource.database

import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbusaws.cloudformation.model.resource.Resource
import com.nimbusframework.nimbusaws.cloudformation.model.resource.ec2.SecurityGroupResource
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.nimbusframework.nimbusaws.annotation.annotations.database.ParsedDatabaseConfig
import com.nimbusframework.nimbusaws.cloudformation.model.resource.DirectAccessResource
import com.nimbusframework.nimbuscore.annotations.database.DatabaseLanguage
import com.nimbusframework.nimbuscore.annotations.database.DatabaseSize

class RdsResource(
    val parsedDatabaseConfig: ParsedDatabaseConfig,
    private val securityGroup: SecurityGroupResource,
    private val subnetGroup: SubnetGroup,
    nimbusState: NimbusState
) : Resource(nimbusState, securityGroup.stage), DirectAccessResource {

    init {
        addDependsOn(securityGroup)
        addDependsOn(subnetGroup)
    }

    override fun toCloudFormation(): JsonObject {
        val dbInstance = JsonObject()
        dbInstance.addProperty("Type", "AWS::RDS::DBInstance")

        val properties = getProperties()
        properties.addProperty("AllocatedStorage", parsedDatabaseConfig.size)
        properties.addProperty("DBInstanceIdentifier", "${parsedDatabaseConfig.name}$stage")
        properties.addProperty("DBInstanceClass", parsedDatabaseConfig.awsDatabaseInstance)
        properties.addProperty("Engine", toEngine(parsedDatabaseConfig.databaseLanguage, parsedDatabaseConfig.awsDatabaseInstance))
        properties.addProperty("PubliclyAccessible", true)
        properties.addProperty("MasterUsername", parsedDatabaseConfig.username)
        properties.addProperty("MasterUserPassword", parsedDatabaseConfig.password)
        properties.addProperty("StorageType", "gp2")
        properties.addProperty("AllocatedStorage", parsedDatabaseConfig.size)

        val securityGroups = JsonArray()
        securityGroups.add(securityGroup.getRef())

        properties.add("VPCSecurityGroups", securityGroups)

        properties.add("DBSubnetGroupName", subnetGroup.getRef())

        dbInstance.add("Properties", properties)

        dbInstance.add("DependsOn", dependsOn)

        return dbInstance
    }

    override fun getName(): String {
        return "RdsInstance${parsedDatabaseConfig.name}"
    }

    private fun toEngine(language: DatabaseLanguage, instanceClass: String): String {
        return if (instanceClass == ParsedDatabaseConfig.toInstanceType(DatabaseSize.FREE)) {
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
