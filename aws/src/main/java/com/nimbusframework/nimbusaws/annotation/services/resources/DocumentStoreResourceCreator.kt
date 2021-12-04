package com.nimbusframework.nimbusaws.annotation.services.resources

import com.nimbusframework.nimbusaws.annotation.annotations.document.DynamoDbDocumentStore
import com.nimbusframework.nimbusaws.annotation.annotations.document.DynamoDbDocumentStores
import com.nimbusframework.nimbusaws.annotation.processor.ProcessingData
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.resource.dynamo.DynamoResource
import com.nimbusframework.nimbuscore.annotations.document.DocumentStoreDefinition
import com.nimbusframework.nimbuscore.annotations.document.DocumentStoreDefinitions
import com.nimbusframework.nimbuscore.annotations.persistent.Key
import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbuscore.wrappers.DynamoConfiguration
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind

class DocumentStoreResourceCreator(
    roundEnvironment: RoundEnvironment,
    cfDocuments: MutableMap<String, CloudFormationFiles>,
    private val processingData: ProcessingData
) : CloudResourceResourceCreator(
    roundEnvironment,
    cfDocuments,
    processingData.nimbusState,
    DocumentStoreDefinition::class.java,
    DocumentStoreDefinitions::class.java,
    DynamoDbDocumentStore::class.java,
    DynamoDbDocumentStores::class.java
) {

    override fun handleAgnosticType(type: Element) {
        val documentStores = type.getAnnotationsByType(DocumentStoreDefinition::class.java)

        for (documentStore in documentStores) {
            for (stage in stageService.determineStages(documentStore.stages)) {
                val tableName = determineTableName(documentStore.tableName, type.simpleName.toString(), stage)
                val dynamoConfiguration = DynamoConfiguration(tableName)
                handleDynamoDbStore(stage, dynamoConfiguration, type)
            }
        }
    }

    override fun handleSpecificType(type: Element) {
        val documentStores = type.getAnnotationsByType(DynamoDbDocumentStore::class.java)

        for (documentStore in documentStores) {
            for (stage in stageService.determineStages(documentStore.stages)) {
                val tableName = determineTableName(documentStore.tableName, type.simpleName.toString(), stage)
                val dynamoConfiguration = DynamoConfiguration(
                    tableName,
                    documentStore.readCapacityUnits,
                    documentStore.writeCapacityUnits,
                    documentStore.existingArn
                )
                handleDynamoDbStore(stage, dynamoConfiguration, type)
            }
        }
    }

    private fun handleDynamoDbStore(stage: String, dynamoConfiguration: DynamoConfiguration, type: Element) {

        val dynamoResource = DynamoResource(dynamoConfiguration, nimbusState, stage)

        val cloudFormationDocuments = cfDocuments.getOrPut(stage) { CloudFormationFiles(nimbusState, stage) }
        val updateResources = cloudFormationDocuments.updateTemplate.resources

        // We need to use reflection on the model class so at runtime we can determine the key column and other columns.
        processingData.classesForReflection.add(type.toString());

        if (dynamoConfiguration.existingArn == "") {
            for (enclosedElement in type.enclosedElements) {
                for (key in enclosedElement.getAnnotationsByType(Key::class.java)) {
                    if (enclosedElement.kind == ElementKind.FIELD) {

                        var columnName = key.columnName
                        if (columnName == "") columnName = enclosedElement.simpleName.toString()

                        val fieldType = enclosedElement.asType()

                        dynamoResource.addHashKey(columnName, fieldType)
                    }
                }
            }
            updateResources.addResource(dynamoResource)
        }

    }
}
