package com.nimbusframework.nimbuscore.annotation.services.resources

import com.nimbusframework.nimbuscore.annotation.annotations.keyvalue.KeyValueStore
import com.nimbusframework.nimbuscore.annotation.annotations.keyvalue.KeyValueStores
import com.nimbusframework.nimbuscore.annotation.wrappers.annotations.datamodel.KeyValueStoreAnnotation
import com.nimbusframework.nimbuscore.cloudformation.CloudFormationDocuments
import com.nimbusframework.nimbuscore.cloudformation.resource.dynamo.DynamoResource
import com.nimbusframework.nimbuscore.persisted.NimbusState
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
            for (stage in keyValueStore.stages) {
                val cloudFormationDocuments = cfDocuments.getOrPut(stage) { CloudFormationDocuments() }
                val updateResources = cloudFormationDocuments.updateResources

                if (keyValueStore.existingArn == "") {
                    val tableName = determineTableName(keyValueStore.tableName, type.simpleName.toString(), stage)
                    val dynamoResource = DynamoResource(tableName, nimbusState, stage)

                    val dataModelAnnotation = KeyValueStoreAnnotation(keyValueStore)
                    val element = dataModelAnnotation.getTypeElement(processingEnvironment)

                    dynamoResource.addHashKeyClass<Any>(keyValueStore.keyName, element)
                    updateResources.addResource(dynamoResource)
                }
            }
        }
    }

}