package com.nimbusframework.nimbuslocal.clients.mock

import com.nimbusframework.nimbuscore.clients.database.DatabaseClient
import com.nimbusframework.nimbuscore.clients.InternalClientBuilder
import com.nimbusframework.nimbuscore.clients.document.DocumentStoreClient
import com.nimbusframework.nimbuscore.clients.file.FileStorageBucketNameAnnotationService
import com.nimbusframework.nimbuscore.clients.file.FileStorageClient
import com.nimbusframework.nimbuscore.clients.function.BasicServerlessFunctionClient
import com.nimbusframework.nimbuscore.clients.function.EnvironmentVariableClient
import com.nimbusframework.nimbuscore.clients.keyvalue.KeyValueStoreClient
import com.nimbusframework.nimbuscore.clients.notification.NotificationClient
import com.nimbusframework.nimbuscore.clients.notification.NotificationTopicAnnotationService
import com.nimbusframework.nimbuscore.clients.queue.QueueClient
import com.nimbusframework.nimbuscore.clients.queue.QueueIdAnnotationService
import com.nimbusframework.nimbuscore.clients.store.TransactionalClient
import com.nimbusframework.nimbuscore.clients.websocket.ServerlessFunctionWebSocketClient
import com.nimbusframework.nimbuslocal.deployment.function.FunctionIdentifier

class MockClientBuilder(
    private val queueClients: Map<String, QueueClient> = mapOf(),
    private val basicFunctionClients: Map<FunctionIdentifier, BasicServerlessFunctionClient> = mapOf(),
    private val basicFunctionInterfaces: Map<Class<*>, Any> = mapOf(),
    private val keyValueStoreClients: Map<Class<*>, KeyValueStoreClient<out Any, out Any>> = mapOf(),
    private val documentStoreClients: Map<Class<*>, DocumentStoreClient<out Any>> = mapOf(),
    private val fileStorageClients: Map<String, FileStorageClient> = mapOf(),
    private val notificationTopicClients: Map<String, NotificationClient> = mapOf(),
    private val databaseClients: Map<Class<*>, DatabaseClient> = mapOf(),
    private val transactionalClient: TransactionalClient? = null,
    private val webSocketClient: ServerlessFunctionWebSocketClient? = null,
    private val environmentVariableClient: EnvironmentVariableClient? = null
): InternalClientBuilder {

    override fun getBasicServerlessFunctionClient(handlerClass: Class<*>, functionName: String): BasicServerlessFunctionClient {
        return basicFunctionClients[FunctionIdentifier(handlerClass, functionName)] ?: error("Missing mock for BasicServerlessFunctionClient")
    }

    override fun <T> getBasicServerlessFunctionInterface(handlerClass: Class<T>): T {
        return basicFunctionInterfaces[handlerClass] as T? ?: error("Missing mock for function interface ${handlerClass.simpleName}")
    }

    override fun <T> getDatabaseClient(databaseObject: Class<T>, stage: String): DatabaseClient {
        return databaseClients[databaseObject] ?: error("Missing mock for DatabaseClient")
    }

    override fun <T> getDocumentStoreClient(document: Class<T>, stage: String): DocumentStoreClient<T> {
        return documentStoreClients[document] as DocumentStoreClient<T>? ?: error("Missing mock for DocumentStoreClient")
    }

    override fun getTransactionalClient(): TransactionalClient {
        return transactionalClient ?: error("Missing mock for TransactionalClient")
    }

    override fun getEnvironmentVariableClient(): EnvironmentVariableClient {
        return environmentVariableClient ?: error("Missing mock for EnvironmentVariableClient")
    }

    override fun getFileStorageClient(bucketClass: Class<*>, stage: String): FileStorageClient {
        return fileStorageClients[FileStorageBucketNameAnnotationService.getBucketName(bucketClass, stage)] ?: error("Missing mock for FileStorageClient")
    }

    override fun <K, V> getKeyValueStoreClient(key: Class<K>, value: Class<V>, stage: String): KeyValueStoreClient<K, V> {
        return keyValueStoreClients[value] as KeyValueStoreClient<K, V>? ?: error("Missing mock for KeyValueStoreClient")
    }

    override fun getNotificationClient(topicClass: Class<*>, stage: String): NotificationClient {
        return notificationTopicClients[NotificationTopicAnnotationService.getTopicName(topicClass, stage)] ?: error("Missing mock for NotificationClient")
    }

    override fun getQueueClient(queueClass: Class<*>, stage: String): QueueClient {
        return queueClients[QueueIdAnnotationService.getQueueId(queueClass, stage)] ?: error("Missing mock for QueueClient")
    }

    override fun getServerlessFunctionWebSocketClient(): ServerlessFunctionWebSocketClient {
        return webSocketClient ?: error("Missing mock for ServerlessFunctionWebSocketClient")
    }
}
