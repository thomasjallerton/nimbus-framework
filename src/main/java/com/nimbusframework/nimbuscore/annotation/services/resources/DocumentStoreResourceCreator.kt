package com.nimbusframework.nimbuscore.annotation.services.resources

import com.nimbusframework.nimbuscore.annotation.annotations.document.DocumentStore
import com.nimbusframework.nimbuscore.annotation.annotations.document.DocumentStores
import com.nimbusframework.nimbuscore.annotation.annotations.persistent.Key
import com.nimbusframework.nimbuscore.cloudformation.CloudFormationDocuments
import com.nimbusframework.nimbuscore.cloudformation.resource.dynamo.DynamoResource
import com.nimbusframework.nimbuscore.persisted.NimbusState
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind

class DocumentStoreResourceCreator(
        roundEnvironment: RoundEnvironment,
        cfDocuments: MutableMap<String, CloudFormationDocuments>,
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
                val dynamoResource = DynamoResource(tableName, nimbusState, stage)

                val cloudFormationDocuments = cfDocuments.getOrPut(stage) { CloudFormationDocuments() }
                val updateResources = cloudFormationDocuments.updateResources

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