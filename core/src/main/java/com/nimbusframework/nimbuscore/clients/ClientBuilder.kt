package com.nimbusframework.nimbuscore.clients

import com.nimbusframework.nimbuscore.clients.database.DatabaseClient
import com.nimbusframework.nimbuscore.clients.database.InternalClientBuilder
import com.nimbusframework.nimbuscore.clients.document.DocumentStoreClient
import com.nimbusframework.nimbuscore.clients.empty.*
import com.nimbusframework.nimbuscore.clients.empty.EmptyDocumentStoreClient
import com.nimbusframework.nimbuscore.clients.file.FileStorageClient
import com.nimbusframework.nimbuscore.clients.function.BasicServerlessFunctionClient
import com.nimbusframework.nimbuscore.clients.function.EnvironmentVariableClient
import com.nimbusframework.nimbuscore.clients.keyvalue.KeyValueStoreClient
import com.nimbusframework.nimbuscore.clients.notification.NotificationClient
import com.nimbusframework.nimbuscore.clients.queue.QueueClient
import com.nimbusframework.nimbuscore.clients.store.TransactionalClient
import com.nimbusframework.nimbuscore.clients.websocket.ServerlessFunctionWebSocketClient

object ClientBuilder {

    private lateinit var internalClientBuilder: InternalClientBuilder

    @JvmStatic
    fun <K, V> getKeyValueStoreClient(key: Class<K>, value: Class<V>): KeyValueStoreClient<K, V> {
        return try {
            internalClientBuilder.getKeyValueStoreClient(key, value, getStage())
        } catch (e: ClassNotFoundException) {
            EmptyKeyValueStoreClient<K, V>()
        } catch (e: NoClassDefFoundError) {
            EmptyKeyValueStoreClient<K, V>()
        }
    }

    @JvmStatic
    fun <T> getDocumentStoreClient(document: Class<T>): DocumentStoreClient<T> {
        return try {
            internalClientBuilder.getDocumentStoreClient(document, getStage())
        } catch (e: ClassNotFoundException) {
            EmptyDocumentStoreClient<T>()
        } catch (e: NoClassDefFoundError) {
            EmptyDocumentStoreClient<T>()
        }
    }

    @JvmStatic
    fun getQueueClient(id: String): QueueClient {
        return try {
            internalClientBuilder.getQueueClient(id)
        } catch (e: ClassNotFoundException) {
            EmptyQueueClient()
        } catch (e: NoClassDefFoundError) {
            EmptyQueueClient()
        }
    }

    @JvmStatic
    fun getQueueClient(queueClass: Class<*>): QueueClient {
        return try {
            internalClientBuilder.getQueueClient(queueClass, getStage())
        } catch (e: ClassNotFoundException) {
            EmptyQueueClient()
        } catch (e: NoClassDefFoundError) {
            EmptyQueueClient()
        }
    }

    @JvmStatic
    fun <T> getDatabaseClient(databaseObject: Class<T>): DatabaseClient {
        return try {
            internalClientBuilder.getDatabaseClient(databaseObject)
        } catch (e: ClassNotFoundException) {
            EmptyDatabaseClient()
        } catch (e: NoClassDefFoundError) {
            EmptyDatabaseClient()
        }
    }

    @JvmStatic
    fun getEnvironmentVariableClient(): EnvironmentVariableClient {
        return try {
            internalClientBuilder.getEnvironmentVariableClient()
        } catch (e: ClassNotFoundException) {
            EmptyEnvironmentVariableClient()
        } catch (e: NoClassDefFoundError) {
            EmptyEnvironmentVariableClient()
        }
    }


    @JvmStatic
    fun getNotificationClient(topic: String): NotificationClient {
        return try {
            internalClientBuilder.getNotificationClient(topic)
        } catch (e: ClassNotFoundException) {
            EmptyNotificationClient()
        } catch (e: NoClassDefFoundError) {
            EmptyNotificationClient()
        }
    }

    @JvmStatic
    fun getNotificationClient(topicClass: Class<*>): NotificationClient {
        return try {
            internalClientBuilder.getNotificationClient(topicClass, getStage())
        } catch (e: ClassNotFoundException) {
            EmptyNotificationClient()
        } catch (e: NoClassDefFoundError) {
            EmptyNotificationClient()
        }
    }

    @JvmStatic
    fun getBasicServerlessFunctionClient(handlerClass: Class<*>, functionName: String): BasicServerlessFunctionClient {
        return try {
            internalClientBuilder.getBasicServerlessFunctionClient(handlerClass, functionName)
        } catch (e: ClassNotFoundException) {
            EmptyBasicServerlessFunctionClient()
        } catch (e: NoClassDefFoundError) {
            EmptyBasicServerlessFunctionClient()
        }
    }

    @JvmStatic
    fun getFileStorageClient(bucketName: String): FileStorageClient {
        return try {
            internalClientBuilder.getFileStorageClient(bucketName, getStage())
        } catch (e: ClassNotFoundException) {
            EmptyFileStorageClient()
        } catch (e: NoClassDefFoundError) {
            EmptyFileStorageClient()
        }
    }

    @JvmStatic
    fun getFileStorageClient(bucketClass: Class<*>): FileStorageClient {
        return try {
            internalClientBuilder.getFileStorageClient(bucketClass, getStage())
        } catch (e: ClassNotFoundException) {
            EmptyFileStorageClient()
        } catch (e: NoClassDefFoundError) {
            EmptyFileStorageClient()
        }
    }

    @JvmStatic
    fun getTransactionalClient(): TransactionalClient {
        return try {
            internalClientBuilder.getTransactionalClient()
        } catch (e: ClassNotFoundException) {
            EmptyTransactionalClient()
        } catch (e: NoClassDefFoundError) {
            EmptyTransactionalClient()
        }
    }

    @JvmStatic
    fun getServerlessFunctionWebSocketClient(): ServerlessFunctionWebSocketClient {
        return try {
            internalClientBuilder.getServerlessFunctionWebSocketClient()
        } catch (e: ClassNotFoundException) {
            EmptyServerlessFunctionWebSocketClient()
        } catch (e: NoClassDefFoundError) {
            EmptyServerlessFunctionWebSocketClient()
        }
    }

    private fun getStage(): String {
        return if (System.getenv().containsKey("NIMBUS_STAGE")) {
            System.getenv("NIMBUS_STAGE")
        } else {
            "dev"
        }
    }
}