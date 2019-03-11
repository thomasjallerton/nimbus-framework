package annotation.services.resources

import annotation.annotations.keyvalue.KeyValueStore
import annotation.annotations.keyvalue.KeyValueStores
import annotation.wrappers.annotations.datamodel.KeyValueStoreAnnotation
import cloudformation.CloudFormationDocuments
import cloudformation.resource.dynamo.DynamoResource
import persisted.NimbusState
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element

class KeyValueStoreResourceCreator(
        roundEnvironment: RoundEnvironment,
        cfDocuments: MutableMap<String, CloudFormationDocuments>,
        private val nimbusState: NimbusState,
        private val processingEnvironment: ProcessingEnvironment
): CloudResourceResourceCreator(
        roundEnvironment,
        cfDocuments,
        KeyValueStore::class.java,
        KeyValueStores::class.java
) {

    override fun handleType(type: Element) {
        val keyValueStores = type.getAnnotationsByType(KeyValueStore::class.java)

        for (keyValueStore in keyValueStores) {
            val cloudFormationDocuments = cfDocuments.getOrPut(keyValueStore.stage) { CloudFormationDocuments() }
            val updateResources = cloudFormationDocuments.updateResources

            if (keyValueStore.existingArn == "") {
                val tableName = determineTableName(keyValueStore.tableName, type.simpleName.toString(), keyValueStore.stage)
                val dynamoResource = DynamoResource(tableName, nimbusState, keyValueStore.stage)

                val dataModelAnnotation = KeyValueStoreAnnotation(keyValueStore)
                val element = dataModelAnnotation.getTypeElement(processingEnvironment)

                dynamoResource.addHashKeyClass<Any>(keyValueStore.keyName, element)
                updateResources.addResource(dynamoResource)
            }
        }
    }

}