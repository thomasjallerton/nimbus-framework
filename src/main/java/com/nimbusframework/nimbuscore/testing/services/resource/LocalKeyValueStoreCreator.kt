package com.nimbusframework.nimbuscore.testing.services.resource

import com.nimbusframework.nimbuscore.annotation.annotations.keyvalue.KeyValueStore
import com.nimbusframework.nimbuscore.clients.keyvalue.AbstractKeyValueStoreClient
import com.nimbusframework.nimbuscore.testing.LocalNimbusDeployment
import com.nimbusframework.nimbuscore.testing.keyvalue.LocalKeyValueStore
import com.nimbusframework.nimbuscore.testing.services.LocalResourceHolder

class LocalKeyValueStoreCreator(
        private val localResourceHolder: LocalResourceHolder,
        private val stage: String
): LocalCreateResourcesHandler {

    override fun createResource(clazz: Class<out Any>) {
        val keyValueStoreAnnotations = clazz.getAnnotationsByType(KeyValueStore::class.java)

        for (keyValueStoreAnnotation in keyValueStoreAnnotations) {
            if (keyValueStoreAnnotation.stages.contains(stage)) {
                val tableName = AbstractKeyValueStoreClient.getTableName(clazz, LocalNimbusDeployment.stage)
                val annotation = clazz.getDeclaredAnnotation(KeyValueStore::class.java)
                localResourceHolder.keyValueStores[tableName] = LocalKeyValueStore(annotation.keyType.java, clazz, LocalNimbusDeployment.stage)
            }
        }
    }
}