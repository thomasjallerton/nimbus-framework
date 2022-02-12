package com.nimbusframework.nimbuslocal.deployment.services.resource

import com.nimbusframework.nimbuscore.annotations.keyvalue.KeyValueStoreDefinition
import com.nimbusframework.nimbuscore.clients.keyvalue.KeyValueStoreAnnotationService
import com.nimbusframework.nimbuslocal.LocalNimbusDeployment
import com.nimbusframework.nimbuslocal.deployment.keyvalue.LocalKeyValueStore
import com.nimbusframework.nimbuslocal.deployment.services.LocalResourceHolder
import com.nimbusframework.nimbuslocal.deployment.services.StageService

class LocalKeyValueStoreCreator(
        private val localResourceHolder: LocalResourceHolder,
        private val stageService: StageService
): LocalCreateResourcesHandler {

    override fun createResource(clazz: Class<out Any>) {
        val keyValueStoreAnnotations = clazz.getAnnotationsByType(KeyValueStoreDefinition::class.java)

        val annotation = stageService.annotationForStage(keyValueStoreAnnotations) { annotation -> annotation.stages}
        if (annotation != null) {
            val tableName = KeyValueStoreAnnotationService.getTableName(clazz, LocalNimbusDeployment.stage)
            val keyTypeAndName = KeyValueStoreAnnotationService.getKeyNameAndType(clazz, LocalNimbusDeployment.stage)

            val localStore = LocalKeyValueStore(
                    annotation.keyType.java,
                    clazz,
                    keyTypeAndName.second,
                    keyTypeAndName.first,
                    tableName,
                    LocalNimbusDeployment.stage)

            localResourceHolder.keyValueStores[clazz] = localStore
            localResourceHolder.webKeyValueStores[tableName] = localStore
        }
    }
}