package annotation.services.resources

import annotation.annotations.keyvalue.KeyValueStore
import annotation.wrappers.annotations.datamodel.KeyValueStoreAnnotation
import cloudformation.CloudFormationDocuments
import cloudformation.resource.dynamo.DynamoResource
import persisted.NimbusState
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment

class KeyValueStoreResourceCreator(
        roundEnvironment: RoundEnvironment,
        cfDocuments: MutableMap<String, CloudFormationDocuments>,
        private val nimbusState: NimbusState,
        private val processingEnvironment: ProcessingEnvironment
): CloudResourceResourceCreator(roundEnvironment, cfDocuments) {

    override fun create() {
        val annotatedElements = roundEnvironment.getElementsAnnotatedWith(KeyValueStore::class.java)

        for (type in annotatedElements) {
            val keyValueStore = type.getAnnotation(KeyValueStore::class.java)

            val cloudFormationDocuments = cfDocuments.getOrPut(keyValueStore.stage) {CloudFormationDocuments()}
            val updateResources = cloudFormationDocuments.updateResources

            if (keyValueStore.existingArn == "") {
                val tableName = determineTableName(keyValueStore.tableName, type.simpleName.toString())
                val dynamoResource = DynamoResource(tableName, nimbusState, keyValueStore.stage)

                val dataModelAnnotation = KeyValueStoreAnnotation(keyValueStore)
                val element = dataModelAnnotation.getTypeElement(processingEnvironment)

                dynamoResource.addHashKeyClass<Any>(keyValueStore.keyName, element)
                updateResources.addResource(dynamoResource)
            }
        }
    }
}