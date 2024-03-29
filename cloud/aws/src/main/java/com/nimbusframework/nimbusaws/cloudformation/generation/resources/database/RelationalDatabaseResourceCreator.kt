package com.nimbusframework.nimbusaws.cloudformation.generation.resources.database

import com.nimbusframework.nimbusaws.annotation.annotations.database.RdsDatabase
import com.nimbusframework.nimbusaws.annotation.annotations.database.RdsDatabases
import com.nimbusframework.nimbusaws.cloudformation.model.CloudFormationFiles
import com.nimbusframework.nimbusaws.annotation.annotations.database.ParsedDatabaseConfig
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.CloudResourceResourceCreator
import com.nimbusframework.nimbusaws.cloudformation.model.resource.database.RdsResource
import com.nimbusframework.nimbusaws.cloudformation.model.resource.database.SubnetGroup
import com.nimbusframework.nimbusaws.cloudformation.model.resource.ec2.*
import com.nimbusframework.nimbuscore.annotations.database.RelationalDatabaseDefinition
import com.nimbusframework.nimbuscore.annotations.database.RelationalDatabaseDefinitions
import com.nimbusframework.nimbuscore.persisted.NimbusState
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element

class RelationalDatabaseResourceCreator(
    roundEnvironment: RoundEnvironment,
    cfDocuments: MutableMap<String, CloudFormationFiles>,
    nimbusState: NimbusState
): CloudResourceResourceCreator(
        roundEnvironment,
        cfDocuments,
        nimbusState,
        RelationalDatabaseDefinition::class.java,
        RelationalDatabaseDefinitions::class.java,
        RdsDatabase::class.java,
        RdsDatabases::class.java
) {

    override fun handleAgnosticType(type: Element) {
        val relationalDatabases = type.getAnnotationsByType(RelationalDatabaseDefinition::class.java)

        for (relationalDatabase in relationalDatabases) {
            handleDatabaseConfiguration(relationalDatabase.stages, ParsedDatabaseConfig.fromRelationDatabase(relationalDatabase))
        }
    }

    override fun handleSpecificType(type: Element) {
        val relationalDatabases = type.getAnnotationsByType(RdsDatabase::class.java)

        for (relationalDatabase in relationalDatabases) {
            handleDatabaseConfiguration(relationalDatabase.stages, ParsedDatabaseConfig.fromRdsDatabase(relationalDatabase))
        }
    }

    private fun handleDatabaseConfiguration(stages: Array<String>, databaseConfiguration: ParsedDatabaseConfig) {
        for (stage in stageService.determineStages(stages)) {
            val cloudFormationDocuments = cfDocuments.getOrPut(stage) { CloudFormationFiles(nimbusState, stage) }
            val updateResources = cloudFormationDocuments.updateTemplate.resources

            val vpc = Vpc(nimbusState, stage)
            val securityGroupResource = SecurityGroupResource(vpc, nimbusState)
            val publicSubnet = Subnet(vpc, "a", "10.0.1.0/24", nimbusState)
            val publicSubnet2 = Subnet(vpc, "b", "10.0.0.0/24", nimbusState)
            val subnets = listOf(publicSubnet, publicSubnet2)

            val subnetGroup = SubnetGroup(subnets, nimbusState, stage)
            val rdsResource = RdsResource(databaseConfiguration, securityGroupResource, subnetGroup, nimbusState)

            val internetGateway = InternetGateway(nimbusState, stage)
            val vpcGatewayAttachment = VpcGatewayAttachment(vpc, internetGateway, nimbusState)
            val table = RouteTable(vpc, nimbusState)
            val route = Route(table, internetGateway, nimbusState)
            val rta1 = RouteTableAssociation(table, publicSubnet, nimbusState)
            val rta2 = RouteTableAssociation(table, publicSubnet2, nimbusState)

            updateResources.addResource(vpc)
            updateResources.addResource(publicSubnet)
            updateResources.addResource(publicSubnet2)
            updateResources.addResource(subnetGroup)
            updateResources.addResource(securityGroupResource)
            updateResources.addDatabase(rdsResource)
            updateResources.addResource(internetGateway)
            updateResources.addResource(vpcGatewayAttachment)
            updateResources.addResource(table)
            updateResources.addResource(route)
            updateResources.addResource(rta1)
            updateResources.addResource(rta2)
        }
    }
}
