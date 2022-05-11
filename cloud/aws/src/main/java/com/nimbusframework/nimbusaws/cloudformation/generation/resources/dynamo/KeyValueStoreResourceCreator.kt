package com.nimbusframework.nimbusaws.cloudformation.generation.resources.dynamo

import com.nimbusframework.nimbusaws.annotation.annotations.keyvalue.DynamoDbKeyValueStore
import com.nimbusframework.nimbusaws.annotation.annotations.keyvalue.DynamoDbKeyValueStores
import com.nimbusframework.nimbusaws.annotation.processor.ProcessingData
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.ClassForReflectionService
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.CloudResourceResourceCreator
import com.nimbusframework.nimbusaws.cloudformation.model.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.model.resource.dynamo.DynamoResource
import com.nimbusframework.nimbusaws.wrappers.annotations.datamodel.DynamoDbKeyValueStoreAnnotation
import com.nimbusframework.nimbuscore.annotations.keyvalue.KeyValueStoreDefinition
import com.nimbusframework.nimbuscore.annotations.keyvalue.KeyValueStoreDefinitions
import com.nimbusframework.nimbuscore.wrappers.DynamoConfiguration
import com.nimbusframework.nimbuscore.wrappers.annotations.datamodel.DataModelAnnotation
import com.nimbusframework.nimbuscore.wrappers.annotations.datamodel.KeyValueStoreAnnotation
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element

class KeyValueStoreResourceCreator(
    roundEnvironment: RoundEnvironment,
    cfDocuments: MutableMap<String, CloudFormationFiles>,
    processingData: ProcessingData,
    private val classForReflectionService: ClassForReflectionService,
    private val processingEnvironment: ProcessingEnvironment
) : CloudResourceResourceCreator(
    roundEnvironment,
    cfDocuments,
    processingData.nimbusState,
    KeyValueStoreDefinition::class.java,
    KeyValueStoreDefinitions::class.java,
    DynamoDbKeyValueStore::class.java,
    DynamoDbKeyValueStores::class.java
) {

    override fun handleAgnosticType(type: Element) {
        val keyValueStores = type.getAnnotationsByType(KeyValueStoreDefinition::class.java)

        for (keyValueStore in keyValueStores) {
            for (stage in stageService.determineStages(keyValueStore.stages)) {
                val tableName = determineTableName(keyValueStore.tableName, type.simpleName.toString(), stage)
                val dataModelAnnotation = KeyValueStoreAnnotation(keyValueStore)
                val dynamoConfiguration = DynamoConfiguration(tableName)
                handleDynamoDbConfiguration(
                    keyValueStore.keyName,
                    stage,
                    dataModelAnnotation,
                    dynamoConfiguration,
                    type
                )
            }
        }
    }

    override fun handleSpecificType(type: Element) {
        val keyValueStores = type.getAnnotationsByType(DynamoDbKeyValueStore::class.java)

        for (keyValueStore in keyValueStores) {
            for (stage in stageService.determineStages(keyValueStore.stages)) {
                val tableName = determineTableName(keyValueStore.tableName, type.simpleName.toString(), stage)
                val dataModelAnnotation = DynamoDbKeyValueStoreAnnotation(keyValueStore)
                val dynamoConfiguration = DynamoConfiguration(
                    tableName,
                    keyValueStore.readCapacityUnits,
                    keyValueStore.writeCapacityUnits
                )
                handleDynamoDbConfiguration(
                    keyValueStore.keyName,
                    stage,
                    dataModelAnnotation,
                    dynamoConfiguration,
                    type
                )
            }
        }
    }

    private fun handleDynamoDbConfiguration(
        keyName: String,
        stage: String,
        dataModelAnnotation: DataModelAnnotation,
        dynamoConfiguration: DynamoConfiguration,
        type: Element
    ) {
        val cloudFormationDocuments = cfDocuments.getOrPut(stage) { CloudFormationFiles(nimbusState, stage) }
        val updateResources = cloudFormationDocuments.updateTemplate.resources

        // We need to use reflection on the model class so at runtime we can determine the key column and other columns.
        classForReflectionService.addClassForReflection(type.asType())

        val dynamoResource = DynamoResource(dynamoConfiguration, nimbusState, stage)

        val element = dataModelAnnotation.getTypeElement(processingEnvironment)

        dynamoResource.addHashKeyClass(keyName, element)
        updateResources.addDynamoResource(type.simpleName.toString(), dynamoResource)

    }

}
