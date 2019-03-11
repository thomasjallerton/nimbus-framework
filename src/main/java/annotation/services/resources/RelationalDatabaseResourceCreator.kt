package annotation.services.resources

import annotation.annotations.database.RelationalDatabase
import annotation.annotations.database.RelationalDatabases
import cloudformation.CloudFormationDocuments
import cloudformation.resource.database.DatabaseConfiguration
import cloudformation.resource.database.RdsResource
import cloudformation.resource.database.SubnetGroup
import cloudformation.resource.ec2.*
import persisted.NimbusState
import java.util.*
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element

class RelationalDatabaseResourceCreator(
        roundEnvironment: RoundEnvironment,
        cfDocuments: MutableMap<String, CloudFormationDocuments>,
        private val nimbusState: NimbusState
): CloudResourceResourceCreator(
        roundEnvironment,
        cfDocuments,
        RelationalDatabase::class.java,
        RelationalDatabases::class.java
) {

    override fun handleType(type: Element) {
        val relationalDatabases = type.getAnnotationsByType(RelationalDatabase::class.java)

        for (relationalDatabase in relationalDatabases) {
            for (stage in relationalDatabase.stages) {
                val cloudFormationDocuments = cfDocuments.getOrPut(stage) { CloudFormationDocuments() }
                val updateResources = cloudFormationDocuments.updateResources

                val databaseConfiguration = DatabaseConfiguration.fromRelationDatabase(relationalDatabase)

                val vpc = Vpc(nimbusState, stage)
                val securityGroupResource = SecurityGroupResource(vpc, nimbusState)
                val publicSubnet = Subnet(vpc, "a", "10.0.1.0/24", nimbusState)
                val publicSubnet2 = Subnet(vpc, "b", "10.0.0.0/24", nimbusState)
                val subnets = LinkedList<Subnet>()
                subnets.add(publicSubnet)
                subnets.add(publicSubnet2)

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
                updateResources.addResource(rdsResource)
                updateResources.addResource(internetGateway)
                updateResources.addResource(vpcGatewayAttachment)
                updateResources.addResource(table)
                updateResources.addResource(route)
                updateResources.addResource(rta1)
                updateResources.addResource(rta2)
            }
        }
    }

}