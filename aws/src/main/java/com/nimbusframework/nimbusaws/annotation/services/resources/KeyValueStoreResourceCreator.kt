package com.nimbusframework.nimbusaws.annotation.services.resources

import com.nimbusframework.nimbuscore.annotations.keyvalue.KeyValueStore
import com.nimbusframework.nimbuscore.annotations.keyvalue.KeyValueStores
import com.nimbusframework.nimbuscore.wrappers.DynamoConfiguration
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.resource.dynamo.DynamoResource
import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbuscore.wrappers.annotations.datamodel.KeyValueStoreAnnotation
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element

class KeyValueStoreResourceCreator(
        roundEnvironment: RoundEnvironment,
        cfDocuments: MutableMap<String, CloudFormationFiles>,
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
            for (stage in keyValueStore.stages) {
                val cloudFormationDocuments = cfDocuments.getOrPut(stage) { CloudFormationFiles(nimbusState, stage) }
                val updateResources = cloudFormationDocuments.updateTemplate.resources

                if (keyValueStore.existingArn == "") {
                    val tableName = determineTableName(keyValueStore.tableName, type.simpleName.toString(), stage)

                    val dynamoConfiguration = DynamoConfiguration(
                            tableName,
                            keyValueStore.readCapacityUnits,
                            keyValueStore.writeCapacityUnits
                    )

                    val dynamoResource = DynamoResource(dynamoConfiguration, nimbusState, stage)

                    val dataModelAnnotation = KeyValueStoreAnnotation(keyValueStore)
                    val element = dataModelAnnotation.getTypeElement(processingEnvironment)

                    dynamoResource.addHashKeyClass<Any>(keyValueStore.keyName, element)
                    updateResources.addResource(dynamoResource)
                }
            }
        }
    }

}