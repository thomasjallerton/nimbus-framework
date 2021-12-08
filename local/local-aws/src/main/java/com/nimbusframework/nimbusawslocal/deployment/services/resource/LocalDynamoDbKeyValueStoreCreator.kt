package com.nimbusframework.nimbusawslocal.deployment.services.resource

import com.nimbusframework.nimbusaws.annotation.annotations.keyvalue.DynamoDbKeyValueStore
import com.nimbusframework.nimbusaws.clients.keyvalue.DynamoDbKeyValueStoreAnnotationService
import com.nimbusframework.nimbuslocal.deployment.keyvalue.LocalKeyValueStore
import com.nimbusframework.nimbuslocal.deployment.services.LocalResourceHolder
import com.nimbusframework.nimbuslocal.deployment.services.StageService
import com.nimbusframework.nimbuslocal.deployment.services.resource.LocalCreateResourcesHandler

class LocalDynamoDbKeyValueStoreCreator(
        private val localResourceHolder: LocalResourceHolder,
        private val stageService: StageService
): LocalCreateResourcesHandler {

    override fun createResource(clazz: Class<out Any>) {
        val keyValueStoreAnnotations = clazz.getAnnotationsByType(DynamoDbKeyValueStore::class.java)

        val deployingAnnotation = stageService.annotationForStage(keyValueStoreAnnotations) {keyValueStore -> keyValueStore.stages }
        if (deployingAnnotation != null) {
            val tableName = DynamoDbKeyValueStoreAnnotationService.getTableName(clazz, stageService.deployingStage)
            val keyTypeAndName = DynamoDbKeyValueStoreAnnotationService.getKeyNameAndType(clazz, stageService.deployingStage)

            localResourceHolder.keyValueStores[clazz] = LocalKeyValueStore(
                    deployingAnnotation.keyType.java,
                    clazz,
                    keyTypeAndName.second,
                    keyTypeAndName.first,
                    tableName,
                    stageService.deployingStage)
        }
    }
}