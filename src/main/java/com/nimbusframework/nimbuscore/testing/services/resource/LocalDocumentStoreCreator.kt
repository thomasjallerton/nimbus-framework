package com.nimbusframework.nimbuscore.testing.services.resource

import com.nimbusframework.nimbuscore.annotation.annotations.document.DocumentStore
import com.nimbusframework.nimbuscore.clients.document.AbstractDocumentStoreClient
import com.nimbusframework.nimbuscore.testing.LocalNimbusDeployment
import com.nimbusframework.nimbuscore.testing.document.LocalDocumentStore
import com.nimbusframework.nimbuscore.testing.services.LocalResourceHolder

class LocalDocumentStoreCreator(
        private val localResourceHolder: LocalResourceHolder,
        private val stage: String
): LocalCreateResourcesHandler {

    override fun createResource(clazz: Class<out Any>) {
        val documentStoreAnnotations = clazz.getAnnotationsByType(DocumentStore::class.java)

        for (documentStoreAnnotation in documentStoreAnnotations) {
            if (documentStoreAnnotation.stages.contains(stage)) {
                val tableName = AbstractDocumentStoreClient.getTableName(clazz, LocalNimbusDeployment.stage)
                localResourceHolder.documentStores[tableName] = LocalDocumentStore(clazz, LocalNimbusDeployment.stage)
            }
        }
    }

}