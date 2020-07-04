package com.nimbusframework.nimbusaws.clients

import com.google.inject.Guice
import com.nimbusframework.nimbusaws.AwsClientModule
import com.nimbusframework.nimbusaws.annotation.annotations.document.DynamoDbDocumentStore
import com.nimbusframework.nimbusaws.annotation.annotations.keyvalue.DynamoDbKeyValueStore
import com.nimbusframework.nimbusaws.clients.document.DocumentStoreClientDynamo
import com.nimbusframework.nimbusaws.clients.document.DynamoDbDocumentStoreAnnotationService
import com.nimbusframework.nimbusaws.clients.dynamo.DynamoTransactionClient
import com.nimbusframework.nimbusaws.clients.file.FileStorageClientS3
import com.nimbusframework.nimbusaws.clients.function.BasicServerlessFunctionClientLambda
import com.nimbusframework.nimbusaws.clients.function.EnvironmentVariableClientLambda
import com.nimbusframework.nimbusaws.clients.keyvalue.DynamoDbKeyValueStoreAnnotationService
import com.nimbusframework.nimbusaws.clients.keyvalue.KeyValueStoreClientDynamo
import com.nimbusframework.nimbusaws.clients.notification.NotificationClientSNS
import com.nimbusframework.nimbusaws.clients.queue.QueueClientSQS
import com.nimbusframework.nimbusaws.clients.rdbms.DatabaseClientRds
import com.nimbusframework.nimbusaws.clients.websocket.ServerlessFunctionWebSocketClientApiGateway
import com.nimbusframework.nimbuscore.annotations.document.DocumentStoreDefinition
import com.nimbusframework.nimbuscore.annotations.file.FileStorageBucketDefinition
import com.nimbusframework.nimbuscore.annotations.keyvalue.KeyValueStoreDefinition
import com.nimbusframework.nimbuscore.clients.database.DatabaseClient
import com.nimbusframework.nimbuscore.clients.database.InternalClientBuilder
import com.nimbusframework.nimbuscore.clients.document.DocumentStoreAnnotationService
import com.nimbusframework.nimbuscore.clients.document.DocumentStoreClient
import com.nimbusframework.nimbuscore.clients.file.FileStorageClient
import com.nimbusframework.nimbuscore.clients.function.BasicServerlessFunctionClient
import com.nimbusframework.nimbuscore.clients.function.EnvironmentVariableClient
import com.nimbusframework.nimbuscore.clients.keyvalue.KeyValueStoreAnnotationService
import com.nimbusframework.nimbuscore.clients.keyvalue.KeyValueStoreClient
import com.nimbusframework.nimbuscore.clients.notification.NotificationClient
import com.nimbusframework.nimbuscore.clients.notification.NotificationTopicAnnotationService
import com.nimbusframework.nimbuscore.clients.queue.QueueClient
import com.nimbusframework.nimbuscore.clients.queue.QueueIdAnnotationService
import com.nimbusframework.nimbuscore.clients.store.TransactionalClient
import com.nimbusframework.nimbuscore.clients.websocket.ServerlessFunctionWebSocketClient

object AwsInternalClientBuilder: InternalClientBuilder {

    private val injector = Guice.createInjector(AwsClientModule())

    override fun getTransactionalClient(): TransactionalClient {
        val transactionalClient = DynamoTransactionClient()
        injector.injectMembers(transactionalClient)
        return transactionalClient
    }

    override fun <K, V> getKeyValueStoreClient(key: Class<K>, value: Class<V>, stage: String): KeyValueStoreClient<K, V> {
        val keyValueStoreClient = when {
            value.isAnnotationPresent(KeyValueStoreDefinition::class.java) -> {
                val tableName = KeyValueStoreAnnotationService.getTableName(value, stage)
                val keyNameAndType = KeyValueStoreAnnotationService.getKeyNameAndType(value, stage)
                KeyValueStoreClientDynamo(key, value, stage, keyNameAndType.first, tableName, keyNameAndType.second)
            }
            value.isAnnotationPresent(DynamoDbKeyValueStore::class.java) -> {
                val tableName = DynamoDbKeyValueStoreAnnotationService.getTableName(value, stage)
                val keyNameAndType = DynamoDbKeyValueStoreAnnotationService.getKeyNameAndType(value, stage)
                KeyValueStoreClientDynamo(key, value, stage, keyNameAndType.first, tableName, keyNameAndType.second)
            }
            else -> throw IllegalStateException("${value.simpleName} is not a known type of Key Value Store (KeyValueStore, DynamoDbKeyValueStore)")
        }
        injector.injectMembers(keyValueStoreClient)
        return keyValueStoreClient
    }

    override fun <T> getDocumentStoreClient(document: Class<T>, stage: String): DocumentStoreClient<T> {
        val documentStoreClient = when {
            document.isAnnotationPresent(DocumentStoreDefinition::class.java) -> {
                val tableName = DocumentStoreAnnotationService.getTableName(document, stage)
                DocumentStoreClientDynamo(document, tableName, stage)
            }
            document.isAnnotationPresent(DynamoDbDocumentStore::class.java) -> {
                val tableName = DynamoDbDocumentStoreAnnotationService.getTableName(document, stage)
                DocumentStoreClientDynamo(document, tableName, stage)
            }
            else -> throw IllegalStateException("${document.simpleName} is not a known type of Document Store (DocumentStore, DynamoDbDocumentStore)")
        }
        injector.injectMembers(documentStoreClient)
        return documentStoreClient
    }

    override fun getQueueClient(id: String): QueueClient {
        val queueClient = QueueClientSQS(id)
        injector.injectMembers(queueClient)
        return queueClient
    }

    override fun getQueueClient(queueClass: Class<*>, stage: String): QueueClient {
        val queueId = QueueIdAnnotationService.getQueueId(queueClass, stage)
        val queueClient = QueueClientSQS(queueId)
        injector.injectMembers(queueClient)
        return queueClient
    }

    override fun <T> getDatabaseClient(databaseObject: Class<T>): DatabaseClient {
        val databaseClient = DatabaseClientRds(databaseObject)
        injector.injectMembers(databaseClient)
        return databaseClient
    }

    override fun getEnvironmentVariableClient(): EnvironmentVariableClient {
        return EnvironmentVariableClientLambda()
    }

    override fun getNotificationClient(topic: String): NotificationClient {
        val notificationClient = NotificationClientSNS(topic)
        injector.injectMembers(notificationClient)
        return notificationClient
    }

    override fun getNotificationClient(topicClass: Class<*>, stage: String): NotificationClient {
        val topic = NotificationTopicAnnotationService.getTopicName(topicClass, stage)
        val notificationClient = NotificationClientSNS(topic)
        injector.injectMembers(notificationClient)
        return notificationClient
    }

    override fun getBasicServerlessFunctionClient(handlerClass: Class<*>, functionName: String): BasicServerlessFunctionClient {
        val basicClient = BasicServerlessFunctionClientLambda(handlerClass, functionName)
        injector.injectMembers(basicClient)
        return basicClient
    }

    override fun getFileStorageClient(bucketName: String, stage: String): FileStorageClient {
        val fileStorageClient = FileStorageClientS3(bucketName, stage)
        injector.injectMembers(fileStorageClient)
        return fileStorageClient
    }

    override fun getFileStorageClient(bucketClass: Class<*>, stage: String): FileStorageClient {
        if (bucketClass.isAnnotationPresent(FileStorageBucketDefinition::class.java)) {
            val bucketName = bucketClass.getAnnotation(FileStorageBucketDefinition::class.java).bucketName
            val fileStorageClient = FileStorageClientS3(bucketName, stage)
            injector.injectMembers(fileStorageClient)
            return fileStorageClient
        } else {
            throw IllegalStateException("${bucketClass.simpleName} is not a known type of File Storage Bucket. (Probably not annotated with @FileStorageBucketDefinition)")
        }
    }

    override fun getServerlessFunctionWebSocketClient(): ServerlessFunctionWebSocketClient {
        val webSocketClientApi = ServerlessFunctionWebSocketClientApiGateway()
        injector.injectMembers(webSocketClientApi)
        return webSocketClientApi
    }


}