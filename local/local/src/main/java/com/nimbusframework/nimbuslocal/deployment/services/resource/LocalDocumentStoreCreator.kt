package com.nimbusframework.nimbuslocal.deployment.services.resource

import com.nimbusframework.nimbuscore.annotations.document.DocumentStoreDefinition
import com.nimbusframework.nimbuscore.clients.document.DocumentStoreAnnotationService
import com.nimbusframework.nimbuslocal.deployment.document.LocalDocumentStore
import com.nimbusframework.nimbuslocal.deployment.services.LocalResourceHolder
import com.nimbusframework.nimbuslocal.deployment.services.StageService

class LocalDocumentStoreCreator(
        private val localResourceHolder: LocalResourceHolder,
        private val stageService: StageService
): LocalCreateResourcesHandler {

    override fun createResource(clazz: Class<out Any>) {
        val documentStoreAnnotations = clazz.getAnnotationsByType(DocumentStoreDefinition::class.java)

        if (stageService.isResourceDeployedInStage(documentStoreAnnotations) {annotation -> annotation.stages}) {
            val tableName = DocumentStoreAnnotationService.getTableName(clazz, stageService.deployingStage)
            val localStore = LocalDocumentStore(clazz, tableName, stageService.deployingStage)

            localResourceHolder.webDocumentStores[tableName] = localStore
            localResourceHolder.documentStores[clazz] = localStore
        }
    }

}