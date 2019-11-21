package com.nimbusframework.nimbusaws.clients

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
import com.nimbusframework.nimbuscore.annotations.document.DocumentStore
import com.nimbusframework.nimbuscore.annotations.keyvalue.KeyValueStore
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
import com.nimbusframework.nimbuscore.clients.queue.QueueClient
import com.nimbusframework.nimbuscore.clients.store.TransactionalClient
import com.nimbusframework.nimbuscore.clients.websocket.ServerlessFunctionWebSocketClient

object AwsInternalClientBuilder: InternalClientBuilder {

    override fun getTransactionalClient(): TransactionalClient {
        return DynamoTransactionClient()
    }

    override fun <K, V> getKeyValueStoreClient(key: Class<K>, value: Class<V>, stage: String): KeyValueStoreClient<K, V> {
        if (value.isAnnotationPresent(KeyValueStore::class.java)) {
            val tableName = KeyValueStoreAnnotationService.getTableName(value, stage)
            val keyNameAndType = KeyValueStoreAnnotationService.getKeyNameAndType(value, stage)
            return KeyValueStoreClientDynamo(key, value, stage, keyNameAndType.first, tableName, keyNameAndType.second)
        } else if (value.isAnnotationPresent(DynamoDbKeyValueStore::class.java)) {
            val tableName = DynamoDbKeyValueStoreAnnotationService.getTableName(value, stage)
            val keyNameAndType = DynamoDbKeyValueStoreAnnotationService.getKeyNameAndType(value, stage)
            return KeyValueStoreClientDynamo(key, value, stage, keyNameAndType.first, tableName, keyNameAndType.second)
        }
        throw IllegalStateException("${value.simpleName} is not a known type of Key Value Store (KeyValueStore, DynamoDbKeyValueStore)")
    }

    override fun <T> getDocumentStoreClient(document: Class<T>, stage: String): DocumentStoreClient<T> {
        if (document.isAnnotationPresent(DocumentStore::class.java)) {
            val tableName = DocumentStoreAnnotationService.getTableName(document, stage)
            return DocumentStoreClientDynamo(document, tableName, stage)
        } else if (document.isAnnotationPresent(DynamoDbDocumentStore::class.java)) {
            val tableName = DynamoDbDocumentStoreAnnotationService.getTableName(document, stage)
            return DocumentStoreClientDynamo(document, tableName, stage)
        }
        throw IllegalStateException("${document.simpleName} is not a known type of Document Store (DocumentStore, DynamoDbDocumentStore)")
    }

    override fun getQueueClient(id: String): QueueClient {
        return QueueClientSQS(id)
    }

    override fun <T> getDatabaseClient(databaseObject: Class<T>): DatabaseClient {
        return DatabaseClientRds(databaseObject)
    }

    override fun getEnvironmentVariableClient(): EnvironmentVariableClient {
        return EnvironmentVariableClientLambda()
    }

    override fun getNotificationClient(topic: String): NotificationClient {
        return NotificationClientSNS(topic)
    }

    override fun getBasicServerlessFunctionClient(handlerClass: Class<*>, functionName: String): BasicServerlessFunctionClient {
        return BasicServerlessFunctionClientLambda(handlerClass, functionName)
    }

    override fun getFileStorageClient(bucketName: String, stage: String): FileStorageClient {
        return FileStorageClientS3(bucketName, stage)
    }

    override fun getServerlessFunctionWebSocketClient(): ServerlessFunctionWebSocketClient {
        return ServerlessFunctionWebSocketClientApiGateway()
    }


}