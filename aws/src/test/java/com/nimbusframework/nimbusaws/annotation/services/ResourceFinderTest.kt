package com.nimbusframework.nimbusaws.annotation.services

import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.TestDataModelAnnotation
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.resource.database.RdsConfiguration
import com.nimbusframework.nimbusaws.cloudformation.resource.database.RdsResource
import com.nimbusframework.nimbusaws.cloudformation.resource.database.SubnetGroup
import com.nimbusframework.nimbusaws.cloudformation.resource.dynamo.DynamoResource
import com.nimbusframework.nimbusaws.cloudformation.resource.ec2.SecurityGroupResource
import com.nimbusframework.nimbusaws.cloudformation.resource.ec2.Subnet
import com.nimbusframework.nimbusaws.cloudformation.resource.ec2.Vpc
import com.nimbusframework.nimbuscore.annotations.NimbusConstants
import com.nimbusframework.nimbuscore.annotations.database.DatabaseLanguage
import com.nimbusframework.nimbuscore.annotations.database.DatabaseSize
import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbuscore.wrappers.DynamoConfiguration
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element

class ResourceFinderTest : AnnotationSpec() {

    private val stage: String = NimbusConstants.stage
    private lateinit var resourceFinder: ResourceFinder
    private lateinit var roundEnvironment: RoundEnvironment
    private lateinit var cfDocuments: MutableMap<String, CloudFormationFiles>
    private lateinit var nimbusState: NimbusState
    private lateinit var methodElement: Element
    private lateinit var compileStateService: CompileStateService

    @BeforeEach
    fun setup() {
        nimbusState = NimbusState()
        cfDocuments = mutableMapOf()
        roundEnvironment = mockk()
        compileStateService = CompileStateService(
                "models/Document.java",
                "models/DynamoDbDocument.java",
                "models/RelationalDatabaseModel.java",
                "models/RdsDatabaseModel.java",
                "models/KeyValue.java",
                "models/DynamoDbKeyValue.java"
        )
    }

    @Test
    fun canFetchDocumentStore() {
        compileStateService.compileObjects {
            methodElement = it.elementUtils.getTypeElement("models.Document")
            resourceFinder = ResourceFinder(cfDocuments, it, nimbusState)

            val cloudFormationFiles = CloudFormationFiles(nimbusState, stage)
            val dynamoConfiguration = DynamoConfiguration("Document$stage")
            val dynamoResource = DynamoResource(dynamoConfiguration, nimbusState, stage)
            cloudFormationFiles.updateTemplate.resources.addResource(dynamoResource)
            cfDocuments[stage] = cloudFormationFiles

            val dataModelAnnotation = TestDataModelAnnotation(it.elementUtils.getTypeElement("models.Document"))

            val foundResource = resourceFinder.getDocumentStoreResource(dataModelAnnotation, methodElement, stage) as DynamoResource

            foundResource shouldBe dynamoResource
        }
    }

    @Test
    fun canFetchDynamoDbDocumentStore() {
        compileStateService.compileObjects {
            methodElement = it.elementUtils.getTypeElement("models.Document")
            resourceFinder = ResourceFinder(cfDocuments, it, nimbusState)
            val cloudFormationFiles = CloudFormationFiles(nimbusState, stage)
            val dynamoConfiguration = DynamoConfiguration("doctable$stage")
            val dynamoResource = DynamoResource(dynamoConfiguration, nimbusState, stage)
            cloudFormationFiles.updateTemplate.resources.addResource(dynamoResource)
            cfDocuments[stage] = cloudFormationFiles

            val dataModelAnnotation = TestDataModelAnnotation(it.elementUtils.getTypeElement("models.DynamoDbDocument"))

            val foundResource = resourceFinder.getDocumentStoreResource(dataModelAnnotation, methodElement, stage) as DynamoResource

            foundResource shouldBe dynamoResource
        }
    }

    @Test
    fun canFetchKeyValueStore() {
        compileStateService.compileObjects {
            methodElement = it.elementUtils.getTypeElement("models.Document")
            resourceFinder = ResourceFinder(cfDocuments, it, nimbusState)
            val cloudFormationFiles = CloudFormationFiles(nimbusState, stage)
            val dynamoConfiguration = DynamoConfiguration("KeyValue$stage")
            val dynamoResource = DynamoResource(dynamoConfiguration, nimbusState, stage)
            cloudFormationFiles.updateTemplate.resources.addResource(dynamoResource)
            cfDocuments[stage] = cloudFormationFiles

            val dataModelAnnotation = TestDataModelAnnotation(it.elementUtils.getTypeElement("models.KeyValue"))

            val foundResource = resourceFinder.getKeyValueStoreResource(dataModelAnnotation, methodElement, stage) as DynamoResource

            foundResource shouldBe dynamoResource
        }
    }

    @Test
    fun canFetchDynamoDbKeyValueStore() {
        compileStateService.compileObjects {
            methodElement = it.elementUtils.getTypeElement("models.Document")
            resourceFinder = ResourceFinder(cfDocuments, it, nimbusState)

            val cloudFormationFiles = CloudFormationFiles(nimbusState, stage)
            val dynamoConfiguration = DynamoConfiguration("keytable$stage")
            val dynamoResource = DynamoResource(dynamoConfiguration, nimbusState, stage)
            cloudFormationFiles.updateTemplate.resources.addResource(dynamoResource)
            cfDocuments[stage] = cloudFormationFiles

            val dataModelAnnotation = TestDataModelAnnotation(it.elementUtils.getTypeElement("models.DynamoDbKeyValue"))

            val foundResource = resourceFinder.getKeyValueStoreResource(dataModelAnnotation, methodElement, stage) as DynamoResource

            foundResource shouldBe dynamoResource
        }
    }

    @Test
    fun canFetchRelationalDatabase() {
        compileStateService.compileObjects {
            methodElement = it.elementUtils.getTypeElement("models.Document")
            resourceFinder = ResourceFinder(cfDocuments, it, nimbusState)
            val cloudFormationFiles = CloudFormationFiles(nimbusState, stage)
            val rdsConfiguration = RdsConfiguration("testRelationalDatabase", "username", "password", DatabaseLanguage.MYSQL, RdsConfiguration.toInstanceType(DatabaseSize.FREE), 30)

            val rdsResource = createRdsResource(rdsConfiguration)
            cloudFormationFiles.updateTemplate.resources.addResource(rdsResource)
            cfDocuments[stage] = cloudFormationFiles

            val dataModelAnnotation = TestDataModelAnnotation(it.elementUtils.getTypeElement("models.RelationalDatabaseModel"))

            val foundResource = resourceFinder.getRelationalDatabaseResource(dataModelAnnotation, methodElement, stage) as RdsResource

            foundResource shouldBe rdsResource
        }
    }

    @Test
    fun canFetchRdsDatabase() {
        compileStateService.compileObjects {
            methodElement = it.elementUtils.getTypeElement("models.Document")
            resourceFinder = ResourceFinder(cfDocuments, it, nimbusState)

            val cloudFormationFiles = CloudFormationFiles(nimbusState, stage)
            val rdsConfiguration = RdsConfiguration("testRdsDatabase", "username", "password", DatabaseLanguage.MYSQL, "micro", 30)

            val rdsResource = createRdsResource(rdsConfiguration)
            cloudFormationFiles.updateTemplate.resources.addResource(rdsResource)
            cfDocuments[stage] = cloudFormationFiles

            val dataModelAnnotation = TestDataModelAnnotation(it.elementUtils.getTypeElement("models.RdsDatabaseModel"))

            val foundResource = resourceFinder.getRelationalDatabaseResource(dataModelAnnotation, methodElement, stage) as RdsResource

            foundResource shouldBe rdsResource
        }
    }

    private fun createRdsResource(databaseConfiguration: RdsConfiguration): RdsResource {
        val vpc = Vpc(nimbusState, stage)
        val securityGroupResource = SecurityGroupResource(vpc, nimbusState)
        val publicSubnet = Subnet(vpc, "a", "10.0.1.0/24", nimbusState)
        val publicSubnet2 = Subnet(vpc, "b", "10.0.0.0/24", nimbusState)
        val subnets = listOf(publicSubnet, publicSubnet2)

        val subnetGroup = SubnetGroup(subnets, nimbusState, stage)
        return RdsResource(databaseConfiguration, securityGroupResource, subnetGroup, nimbusState)
    }
}