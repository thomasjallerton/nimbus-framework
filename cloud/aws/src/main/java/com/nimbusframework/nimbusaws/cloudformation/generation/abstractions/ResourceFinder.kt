package com.nimbusframework.nimbusaws.cloudformation.generation.abstractions

import com.nimbusframework.nimbusaws.annotation.annotations.database.RdsDatabase
import com.nimbusframework.nimbusaws.annotation.annotations.document.DynamoDbDocumentStore
import com.nimbusframework.nimbusaws.annotation.annotations.keyvalue.DynamoDbKeyValueStore
import com.nimbusframework.nimbuscore.annotations.database.RelationalDatabaseDefinition
import com.nimbusframework.nimbuscore.annotations.document.DocumentStoreDefinition
import com.nimbusframework.nimbuscore.annotations.keyvalue.KeyValueStoreDefinition
import com.nimbusframework.nimbusaws.cloudformation.model.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.model.resource.Resource
import com.nimbusframework.nimbusaws.cloudformation.model.resource.database.RdsResource
import com.nimbusframework.nimbusaws.cloudformation.model.resource.file.FileBucketResource
import com.nimbusframework.nimbusaws.cloudformation.model.resource.notification.SnsTopicResource
import com.nimbusframework.nimbusaws.cloudformation.model.resource.queue.QueueResource
import com.nimbusframework.nimbuscore.annotations.file.FileStorageBucketDefinition
import com.nimbusframework.nimbuscore.annotations.notification.NotificationTopicDefinition
import com.nimbusframework.nimbuscore.annotations.queue.QueueDefinition
import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbuscore.wrappers.annotations.datamodel.DataModelAnnotation
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.tools.Diagnostic

class ResourceFinder(private val resourceCollections: Map<String, CloudFormationFiles>, private val processingEnv: ProcessingEnvironment, nimbusState: NimbusState) {

    private val messager = processingEnv.messager
    private val stageService = StageService(nimbusState.defaultStages)

    fun getDocumentStoreResource(dataModelAnnotation: DataModelAnnotation, serverlessMethod: Element, dataModelStage: String): Resource? {
        try {
            val typeElement = dataModelAnnotation.getTypeElement(processingEnv)
            val documentStores = typeElement.getAnnotationsByType(DocumentStoreDefinition::class.java)
            for (documentStore in documentStores) {
                for (stage in stageService.determineStages(documentStore.stages)) {
                    if (stage == dataModelStage) {
                        return resourceCollections.getValue(stage).updateTemplate.resources.getDynamoResource(typeElement.simpleName.toString())
                    }
                }
            }
            val dynamoStores = typeElement.getAnnotationsByType(DynamoDbDocumentStore::class.java)
            for (dynamoStore in dynamoStores) {
                for (stage in stageService.determineStages(dynamoStore.stages)) {
                    if (stage == dataModelStage) {
                        return resourceCollections.getValue(stage).updateTemplate.resources.getDynamoResource(typeElement.simpleName.toString())
                    }
                }
            }
            messager.printMessage(Diagnostic.Kind.ERROR, "Could not find DocumentStore on @UsesDocumentStoreHandler dataModel with same stage")
            return null
        } catch (e: NullPointerException) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Input class expected to be annotated with DocumentStoreDefinition but isn't", serverlessMethod)
            return null
        }
    }

    fun getKeyValueStoreResource(dataModelAnnotation: DataModelAnnotation, serverlessMethod: Element, dataModelStage: String): Resource? {
        try {
            val typeElement = dataModelAnnotation.getTypeElement(processingEnv)
            val keyValueStores = typeElement.getAnnotationsByType(KeyValueStoreDefinition::class.java)
            for (keyValueStore in keyValueStores) {
                for (stage in stageService.determineStages(keyValueStore.stages)) {
                    if (stage == dataModelStage) {
                        return resourceCollections.getValue(stage).updateTemplate.resources.getDynamoResource(typeElement.simpleName.toString())
                    }
                }
            }
            val dynamoDbKeyValueStores = typeElement.getAnnotationsByType(DynamoDbKeyValueStore::class.java)
            for (dynamoDbKeyValueStore in dynamoDbKeyValueStores) {
                for (stage in stageService.determineStages(dynamoDbKeyValueStore.stages)) {
                    if (stage == dataModelStage) {
                        return resourceCollections.getValue(stage).updateTemplate.resources.getDynamoResource(typeElement.simpleName.toString())
                    }
                }
            }
            messager.printMessage(Diagnostic.Kind.ERROR, "Could not find KeyValueStore on @UsesKeyValueStore dataModel with same stage")
            return null
        } catch (e: NullPointerException) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Input class expected to be annotated with KeyValueStoreDefinition but isn't", serverlessMethod)
            return null
        }
    }

    fun getRelationalDatabaseResource(dataModelAnnotation: DataModelAnnotation, serverlessMethod: Element, dataModelStage: String): RdsResource? {
        val typeElement = dataModelAnnotation.getTypeElement(processingEnv)
        return try {
            val relationalDatabase = typeElement.getAnnotation(RelationalDatabaseDefinition::class.java)
            resourceCollections.getValue(dataModelStage).updateTemplate.resources.getDatabase(relationalDatabase)
        } catch (e: NullPointerException) {
            try {
                val rdsDatabase = typeElement.getAnnotation(RdsDatabase::class.java)
                resourceCollections.getValue(dataModelStage).updateTemplate.resources.getDatabase(rdsDatabase)
            } catch (e: NullPointerException) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Input class expected to be annotated with RelationalDatabaseDefinition but isn't", serverlessMethod)
                null
            }
        }
    }

    fun getNotificationTopicResource(dataModelAnnotation: DataModelAnnotation, serverlessMethod: Element, dataModelStage: String): SnsTopicResource? {
        val typeElement = dataModelAnnotation.getTypeElement(processingEnv)
        return try {
            val notificationTopicDefinition = typeElement.getAnnotation(NotificationTopicDefinition::class.java)
            resourceCollections.getValue(dataModelStage).updateTemplate.resources.getSnsResource(notificationTopicDefinition)
        } catch (e: NullPointerException) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Input class expected to be annotated with NotificationTopicDefinition but isn't", serverlessMethod)
            null
        }
    }

    fun getQueueResource(dataModelAnnotation: DataModelAnnotation, serverlessMethod: Element, dataModelStage: String): QueueResource? {
        val typeElement = dataModelAnnotation.getTypeElement(processingEnv)
        return try {
            val queueDefinition = typeElement.getAnnotation(QueueDefinition::class.java)
            resourceCollections.getValue(dataModelStage).updateTemplate.resources.getQueue(queueDefinition)
        } catch (e: NullPointerException) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Input class expected to be annotated with QueueDefinition but isn't", serverlessMethod)
            null
        }
    }

    fun getFileStorageBucketResource(dataModelAnnotation: DataModelAnnotation, serverlessMethod: Element, dataModelStage: String): FileBucketResource? {
        val typeElement = dataModelAnnotation.getTypeElement(processingEnv)
        return try {
            val fileStorageBucketDefinition = typeElement.getAnnotation(FileStorageBucketDefinition::class.java)
            resourceCollections.getValue(dataModelStage).updateTemplate.resources.getFileStorageBucket(fileStorageBucketDefinition)
        } catch (e: NullPointerException) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Input class expected to be annotated with FileStorageBucketDefinition but isn't", serverlessMethod)
            null
        }
    }

}
