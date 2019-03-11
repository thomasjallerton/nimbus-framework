package annotation.services.resources

import annotation.annotations.document.DocumentStore
import annotation.annotations.document.DocumentStores
import annotation.annotations.persistent.Key
import cloudformation.CloudFormationDocuments
import cloudformation.resource.dynamo.DynamoResource
import persisted.NimbusState
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind

class DocumentStoreResourceCreator(
        roundEnvironment: RoundEnvironment,
        cfDocuments: MutableMap<String, CloudFormationDocuments>,
        private val nimbusState: NimbusState
): CloudResourceResourceCreator(
        roundEnvironment,
        cfDocuments,
        DocumentStore::class.java,
        DocumentStores::class.java
) {

    override fun handleType(type: Element) {
        val documentStores = type.getAnnotationsByType(DocumentStore::class.java)

        for (documentStore in documentStores) {
            val tableName = determineTableName(documentStore.tableName, type.simpleName.toString(), documentStore.stage)
            val dynamoResource = DynamoResource(tableName, nimbusState, documentStore.stage)

            val cloudFormationDocuments = cfDocuments.getOrPut(documentStore.stage) { CloudFormationDocuments() }
            val updateResources = cloudFormationDocuments.updateResources

            if (documentStore.existingArn == "") {

                if (type.kind == ElementKind.CLASS) {

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
                }
                updateResources.addResource(dynamoResource)
            }
        }
    }

}