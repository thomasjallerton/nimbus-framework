package com.nimbusframework.nimbusaws.annotation.services.resources

import com.nimbusframework.nimbuscore.annotations.document.DocumentStore
import com.nimbusframework.nimbuscore.annotations.document.DocumentStores
import com.nimbusframework.nimbuscore.annotations.persistent.Key
import com.nimbusframework.nimbuscore.wrappers.DynamoConfiguration
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.resource.dynamo.DynamoResource
import com.nimbusframework.nimbuscore.persisted.NimbusState
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind

class DocumentStoreResourceCreator(
        roundEnvironment: RoundEnvironment,
        cfDocuments: MutableMap<String, CloudFormationFiles>,
        private val nimbusState: NimbusState
) : CloudResourceResourceCreator(
        roundEnvironment,
        cfDocuments,
        DocumentStore::class.java,
        DocumentStores::class.java
) {

    override fun handleType(type: Element) {
        val documentStores = type.getAnnotationsByType(DocumentStore::class.java)

        for (documentStore in documentStores) {
            for (stage in documentStore.stages) {
                val tableName = determineTableName(documentStore.tableName, type.simpleName.toString(), stage)

                val dynamoConfiguration = DynamoConfiguration(
                        tableName,
                        documentStore.readCapacityUnits,
                        documentStore.writeCapacityUnits
                )

                val dynamoResource = DynamoResource(dynamoConfiguration, nimbusState, stage)

                val cloudFormationDocuments = cfDocuments.getOrPut(stage) { CloudFormationFiles(nimbusState, stage) }
                val updateResources = cloudFormationDocuments.updateTemplate.resources

                if (documentStore.existingArn == "") {
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
    }

}