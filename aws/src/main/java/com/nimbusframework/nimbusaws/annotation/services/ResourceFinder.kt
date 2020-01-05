package com.nimbusframework.nimbusaws.annotation.services

import com.nimbusframework.nimbusaws.annotation.annotations.database.RdsDatabase
import com.nimbusframework.nimbusaws.annotation.annotations.document.DynamoDbDocumentStore
import com.nimbusframework.nimbusaws.annotation.annotations.keyvalue.DynamoDbKeyValueStore
import com.nimbusframework.nimbuscore.annotations.database.RelationalDatabaseDefinition
import com.nimbusframework.nimbuscore.annotations.document.DocumentStoreDefinition
import com.nimbusframework.nimbuscore.annotations.keyvalue.KeyValueStoreDefinition
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.resource.ExistingResource
import com.nimbusframework.nimbusaws.cloudformation.resource.Resource
import com.nimbusframework.nimbusaws.cloudformation.resource.database.RdsResource
import com.nimbusframework.nimbusaws.cloudformation.resource.file.FileBucket
import com.nimbusframework.nimbusaws.cloudformation.resource.notification.SnsTopicResource
import com.nimbusframework.nimbusaws.cloudformation.resource.queue.QueueResource
import com.nimbusframework.nimbuscore.annotations.file.FileStorageBucketDefinition
import com.nimbusframework.nimbuscore.annotations.notification.NotificationTopicDefinition
import com.nimbusframework.nimbuscore.annotations.queue.QueueDefinition
import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbuscore.wrappers.annotations.datamodel.DataModelAnnotation
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.tools.Diagnostic

class ResourceFinder(private val resourceCollections: Map<String, CloudFormationFiles>, private val processingEnv: ProcessingEnvironment, private val nimbusState: NimbusState) {

    private val messager = processingEnv.messager

    fun getDocumentStoreResource(dataModelAnnotation: DataModelAnnotation, serverlessMethod: Element, dataModelStage: String): Resource? {
        try {
            val typeElement = dataModelAnnotation.getTypeElement(processingEnv)
            val documentStores = typeElement.getAnnotationsByType(DocumentStoreDefinition::class.java)
            for (documentStore in documentStores) {
                for (stage in documentStore.stages) {
                    if (stage == dataModelStage) {
                        return getStoreResource("", documentStore.tableName, typeElement.simpleName.toString(), dataModelStage)
                    }
                }
            }
            val dynamoStores = typeElement.getAnnotationsByType(DynamoDbDocumentStore::class.java)
            for (dynamoStore in dynamoStores) {
                for (stage in dynamoStore.stages) {
                    if (stage == dataModelStage) {
                        return getStoreResource(dynamoStore.existingArn, dynamoStore.tableName, typeElement.simpleName.toString(), dataModelStage)
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
                for (stage in keyValueStore.stages) {
                    if (stage == dataModelStage) {
                        return getStoreResource("", keyValueStore.tableName, typeElement.simpleName.toString(), dataModelStage)
                    }
                }
            }
            val dynamoDbKeyValueStores = typeElement.getAnnotationsByType(DynamoDbKeyValueStore::class.java)
            for (keyValueStore in dynamoDbKeyValueStores) {
                for (stage in keyValueStore.stages) {
                    if (stage == dataModelStage) {
                        return getStoreResource(keyValueStore.existingArn, keyValueStore.tableName, typeElement.simpleName.toString(), dataModelStage)
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
            resourceCollections.getValue(dataModelStage).updateTemplate.resources.get("RdsInstance${relationalDatabase.name}") as RdsResource?
        } catch (e: NullPointerException) {
            try {
                val rdsDatabase = typeElement.getAnnotation(RdsDatabase::class.java)
                resourceCollections.getValue(dataModelStage).updateTemplate.resources.get("RdsInstance${rdsDatabase.name}") as RdsResource?
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
            resourceCollections.getValue(dataModelStage).updateTemplate.resources.get("SNSTopic${notificationTopicDefinition.topicName}") as SnsTopicResource?
        } catch (e: NullPointerException) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Input class expected to be annotated with NotificationTopicDefinition but isn't", serverlessMethod)
            null
        }
    }

    fun getQueueResource(dataModelAnnotation: DataModelAnnotation, serverlessMethod: Element, dataModelStage: String): QueueResource? {
        val typeElement = dataModelAnnotation.getTypeElement(processingEnv)
        return try {
            val queueDefinition = typeElement.getAnnotation(QueueDefinition::class.java)
            resourceCollections.getValue(dataModelStage).updateTemplate.resources.get("SQSQueue${queueDefinition.queueId}") as QueueResource?
        } catch (e: NullPointerException) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Input class expected to be annotated with QueueDefinition but isn't", serverlessMethod)
            null
        }
    }

    fun getFileStorageBucketResource(dataModelAnnotation: DataModelAnnotation, serverlessMethod: Element, dataModelStage: String): FileBucket? {
        val typeElement = dataModelAnnotation.getTypeElement(processingEnv)
        return try {
            val fileStorageBucketDefinition = typeElement.getAnnotation(FileStorageBucketDefinition::class.java)
            resourceCollections.getValue(dataModelStage).updateTemplate.resources.get("${nimbusState.projectName}${fileStorageBucketDefinition.bucketName}FileBucket") as FileBucket?
        } catch (e: NullPointerException) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Input class expected to be annotated with FileStorageBucketDefinition but isn't", serverlessMethod)
            null
        }
    }

    private fun getStoreResource(existingArn: String, tableName: String, elementName: String, stage: String): Resource? {
        return if (existingArn == "") {
            return resourceCollections.getValue(stage).updateTemplate.resources.get(determineTableName(tableName, elementName, stage))
        } else {
            ExistingResource(existingArn, nimbusState, stage)
        }
    }

    private fun determineTableName(givenName: String, className: String, stage: String): String {
        return if (givenName == "") {
            "$className$stage"
        } else {
            "$givenName$stage"
        }
    }
}