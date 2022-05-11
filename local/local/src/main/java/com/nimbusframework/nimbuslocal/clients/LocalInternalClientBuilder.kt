package com.nimbusframework.nimbuslocal.clients

import com.nimbusframework.nimbuscore.clients.database.DatabaseClient
import com.nimbusframework.nimbuscore.clients.InternalClientBuilder
import com.nimbusframework.nimbuscore.clients.document.DocumentStoreClient
import com.nimbusframework.nimbuscore.clients.file.FileStorageClient
import com.nimbusframework.nimbuscore.clients.function.BasicServerlessFunctionClient
import com.nimbusframework.nimbuscore.clients.function.EnvironmentVariableClient
import com.nimbusframework.nimbuscore.clients.keyvalue.KeyValueStoreClient
import com.nimbusframework.nimbuscore.clients.notification.NotificationClient
import com.nimbusframework.nimbuscore.clients.queue.QueueClient
import com.nimbusframework.nimbuscore.clients.store.TransactionalClient
import com.nimbusframework.nimbuscore.clients.websocket.ServerlessFunctionWebSocketClient

object LocalInternalClientBuilder: InternalClientBuilder {

    override fun getBasicServerlessFunctionClient(handlerClass: Class<*>, functionName: String): BasicServerlessFunctionClient {
        return BasicServerlessFunctionClientLocal(handlerClass, functionName)
    }

    override fun <T> getBasicServerlessFunctionInterface(handlerClass: Class<T>): T {
        return Class.forName(handlerClass.canonicalName + "Serverless").getDeclaredConstructor().newInstance() as T
    }

    override fun <T> getDatabaseClient(databaseObject: Class<T>, stage: String): DatabaseClient {
        return DatabaseClientLocal(databaseObject)
    }

    override fun <T> getDocumentStoreClient(document: Class<T>, stage: String): DocumentStoreClient<T> {
        return DocumentStoreClientLocal(document)
    }

    override fun getTransactionalClient(): TransactionalClient {
        return TransactionalClientLocal()
    }

    override fun getEnvironmentVariableClient(): EnvironmentVariableClient {
        return EnvironmentVariableClientLocal()
    }

    override fun getFileStorageClient(bucketClass: Class<*>, stage: String): FileStorageClient {
        return FileStorageClientLocal(bucketClass, stage)
    }

    override fun <K, V> getKeyValueStoreClient(key: Class<K>, value: Class<V>, stage: String): KeyValueStoreClient<K, V> {
        return KeyValueStoreClientLocal(value)
    }

    override fun getNotificationClient(topicClass: Class<*>, stage: String): NotificationClient {
        return NotificationClientLocal(topicClass, stage)
    }

    override fun getQueueClient(queueClass: Class<*>, stage: String): QueueClient {
        return QueueClientLocal(queueClass, stage)
    }

    override fun getServerlessFunctionWebSocketClient(): ServerlessFunctionWebSocketClient {
        return ServerlessFunctionWebsocketClientLocal()
    }
}
