package com.nimbusframework.nimbusawslocal.deployment.services.resource

import com.nimbusframework.nimbusaws.annotation.annotations.document.DynamoDbDocumentStore
import com.nimbusframework.nimbusaws.clients.document.DynamoDbDocumentStoreAnnotationService
import com.nimbusframework.nimbuslocal.deployment.document.LocalDocumentStore
import com.nimbusframework.nimbuslocal.deployment.services.LocalResourceHolder
import com.nimbusframework.nimbuslocal.deployment.services.StageService
import com.nimbusframework.nimbuslocal.deployment.services.resource.LocalCreateResourcesHandler

class LocalDynamoDbDocumentStoreCreator(
        private val localResourceHolder: LocalResourceHolder,
        private val stageService: StageService
): LocalCreateResourcesHandler {

    override fun createResource(clazz: Class<out Any>) {
        val documentStoreAnnotations = clazz.getAnnotationsByType(DynamoDbDocumentStore::class.java)

        if (stageService.isResourceDeployedInStage(documentStoreAnnotations) {dynamoDbDocumentStore -> dynamoDbDocumentStore.stages }) {
            val tableName = DynamoDbDocumentStoreAnnotationService.getTableName(clazz, stageService.deployingStage)
            localResourceHolder.documentStores[clazz] = LocalDocumentStore(clazz, tableName, stageService.deployingStage)
            localResourceHolder.webDocumentStores[tableName] = LocalDocumentStore(clazz, tableName, stageService.deployingStage)
        }

    }

}