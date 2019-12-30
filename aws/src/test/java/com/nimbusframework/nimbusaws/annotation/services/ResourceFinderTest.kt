package com.nimbusframework.nimbusaws.annotation.services

import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.TestDataModelAnnotation
import com.nimbusframework.nimbusaws.annotation.services.resources.RelationalDatabaseResourceCreator
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.resource.database.RdsConfiguration
import com.nimbusframework.nimbusaws.cloudformation.resource.database.RdsResource
import com.nimbusframework.nimbusaws.cloudformation.resource.database.SubnetGroup
import com.nimbusframework.nimbusaws.cloudformation.resource.dynamo.DynamoResource
import com.nimbusframework.nimbusaws.cloudformation.resource.ec2.SecurityGroupResource
import com.nimbusframework.nimbusaws.cloudformation.resource.ec2.Subnet
import com.nimbusframework.nimbusaws.cloudformation.resource.ec2.Vpc
import com.nimbusframework.nimbuscore.annotations.database.DatabaseLanguage
import com.nimbusframework.nimbuscore.annotations.database.DatabaseSize
import com.nimbusframework.nimbuscore.annotations.function.DocumentStoreServerlessFunction
import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbuscore.wrappers.DynamoConfiguration
import com.nimbusframework.nimbuscore.wrappers.annotations.datamodel.DocumentStoreServerlessFunctionAnnotation
import io.kotlintest.should
import io.kotlintest.specs.AnnotationSpec
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.mockk.mockk
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.util.Elements

class ResourceFinderTest : AnnotationSpec() {

    private val stage: String = "dev"
    private lateinit var resourceFinder: ResourceFinder
    private lateinit var roundEnvironment: RoundEnvironment
    private lateinit var cfDocuments: MutableMap<String, CloudFormationFiles>
    private lateinit var nimbusState: NimbusState
    private lateinit var elements: Elements
    private lateinit var methodElement: Element

    @BeforeEach
    fun setup() {
        nimbusState = NimbusState()
        cfDocuments = mutableMapOf()
        roundEnvironment = mockk()
        val compileState = CompileStateService(
                "models/Document.java",
                "models/DynamoDbDocument.java",
                "models/RelationalDatabaseModel.java",
                "models/RdsDatabaseModel.java",
                "models/KeyValue.java",
                "models/DynamoDbKeyValue.java"
        )
        this.elements = compileState.elements
        methodElement = elements.getTypeElement("models.Document")

        resourceFinder = ResourceFinder(cfDocuments, compileState.processingEnvironment, nimbusState)
    }

    @Test
    fun canFetchDocumentStore() {
        val cloudFormationFiles = CloudFormationFiles(nimbusState, stage)
        val dynamoConfiguration = DynamoConfiguration("Document$stage")
        val dynamoResource = DynamoResource(dynamoConfiguration, nimbusState, stage)
        cloudFormationFiles.updateTemplate.resources.addResource(dynamoResource)
        cfDocuments[stage] = cloudFormationFiles

        val dataModelAnnotation = TestDataModelAnnotation(elements.getTypeElement("models.Document"), arrayOf(stage))

        val foundResource = resourceFinder.getDocumentStoreResource(dataModelAnnotation, methodElement, stage) as DynamoResource

        foundResource shouldBe dynamoResource
    }

    @Test
    fun canFetchDynamoDbDocumentStore() {
        val cloudFormationFiles = CloudFormationFiles(nimbusState, stage)
        val dynamoConfiguration = DynamoConfiguration("doctable$stage")
        val dynamoResource = DynamoResource(dynamoConfiguration, nimbusState, stage)
        cloudFormationFiles.updateTemplate.resources.addResource(dynamoResource)
        cfDocuments[stage] = cloudFormationFiles

        val dataModelAnnotation = TestDataModelAnnotation(elements.getTypeElement("models.DynamoDbDocument"), arrayOf(stage))

        val foundResource = resourceFinder.getDocumentStoreResource(dataModelAnnotation, methodElement, stage) as DynamoResource

        foundResource shouldBe dynamoResource
    }

    @Test
    fun canFetchKeyValueStore() {
        val cloudFormationFiles = CloudFormationFiles(nimbusState, stage)
        val dynamoConfiguration = DynamoConfiguration("KeyValue$stage")
        val dynamoResource = DynamoResource(dynamoConfiguration, nimbusState, stage)
        cloudFormationFiles.updateTemplate.resources.addResource(dynamoResource)
        cfDocuments[stage] = cloudFormationFiles

        val dataModelAnnotation = TestDataModelAnnotation(elements.getTypeElement("models.KeyValue"), arrayOf(stage))

        val foundResource = resourceFinder.getKeyValueStoreResource(dataModelAnnotation, methodElement, stage) as DynamoResource

        foundResource shouldBe dynamoResource
    }

    @Test
    fun canFetchDynamoDbKeyValueStore() {
        val cloudFormationFiles = CloudFormationFiles(nimbusState, stage)
        val dynamoConfiguration = DynamoConfiguration("keytable$stage")
        val dynamoResource = DynamoResource(dynamoConfiguration, nimbusState, stage)
        cloudFormationFiles.updateTemplate.resources.addResource(dynamoResource)
        cfDocuments[stage] = cloudFormationFiles

        val dataModelAnnotation = TestDataModelAnnotation(elements.getTypeElement("models.DynamoDbKeyValue"), arrayOf(stage))

        val foundResource = resourceFinder.getKeyValueStoreResource(dataModelAnnotation, methodElement, stage) as DynamoResource

        foundResource shouldBe dynamoResource
    }

    @Test
    fun canFetchRelationalDatabase() {
        val cloudFormationFiles = CloudFormationFiles(nimbusState, stage)
        val rdsConfiguration = RdsConfiguration("testRelationalDatabase", "username", "password", DatabaseLanguage.MYSQL, RdsConfiguration.toInstanceType(DatabaseSize.FREE), 30)

        val rdsResource = createRdsResource(rdsConfiguration)
        cloudFormationFiles.updateTemplate.resources.addResource(rdsResource)
        cfDocuments[stage] = cloudFormationFiles

        val dataModelAnnotation = TestDataModelAnnotation(elements.getTypeElement("models.RelationalDatabaseModel"), arrayOf(stage))

        val foundResource = resourceFinder.getRelationalDatabaseResource(dataModelAnnotation, methodElement, stage) as RdsResource

        foundResource shouldBe rdsResource
    }

    @Test
    fun canFetchRdsDatabase() {
        val cloudFormationFiles = CloudFormationFiles(nimbusState, stage)
        val rdsConfiguration = RdsConfiguration("testRdsDatabase", "username", "password", DatabaseLanguage.MYSQL, "micro", 30)

        val rdsResource = createRdsResource(rdsConfiguration)
        cloudFormationFiles.updateTemplate.resources.addResource(rdsResource)
        cfDocuments[stage] = cloudFormationFiles

        val dataModelAnnotation = TestDataModelAnnotation(elements.getTypeElement("models.RdsDatabaseModel"), arrayOf(stage))

        val foundResource = resourceFinder.getRelationalDatabaseResource(dataModelAnnotation, methodElement, stage) as RdsResource

        foundResource shouldBe rdsResource
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