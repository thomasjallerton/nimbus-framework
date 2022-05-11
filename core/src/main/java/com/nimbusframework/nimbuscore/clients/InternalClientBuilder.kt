package com.nimbusframework.nimbuscore.clients

import com.nimbusframework.nimbuscore.clients.database.DatabaseClient
import com.nimbusframework.nimbuscore.clients.document.DocumentStoreClient
import com.nimbusframework.nimbuscore.clients.file.FileStorageClient
import com.nimbusframework.nimbuscore.clients.function.BasicServerlessFunctionClient
import com.nimbusframework.nimbuscore.clients.function.EnvironmentVariableClient
import com.nimbusframework.nimbuscore.clients.keyvalue.KeyValueStoreClient
import com.nimbusframework.nimbuscore.clients.notification.NotificationClient
import com.nimbusframework.nimbuscore.clients.queue.QueueClient
import com.nimbusframework.nimbuscore.clients.store.TransactionalClient
import com.nimbusframework.nimbuscore.clients.websocket.ServerlessFunctionWebSocketClient

interface InternalClientBuilder {

    fun <K, V> getKeyValueStoreClient(key: Class<K>, value: Class<V>, stage: String): KeyValueStoreClient<K, V>

    fun <T> getDocumentStoreClient(document: Class<T>, stage: String): DocumentStoreClient<T>

    fun getQueueClient(queueClass: Class<*>, stage: String): QueueClient

    fun <T> getDatabaseClient(databaseObject: Class<T>, stage: String): DatabaseClient

    fun getEnvironmentVariableClient(): EnvironmentVariableClient

    fun getNotificationClient(topicClass: Class<*>, stage: String): NotificationClient

    fun getBasicServerlessFunctionClient(handlerClass: Class<*>, functionName: String): BasicServerlessFunctionClient

    fun getFileStorageClient(bucketClass: Class<*>, stage: String): FileStorageClient

    fun getServerlessFunctionWebSocketClient(): ServerlessFunctionWebSocketClient

    fun getTransactionalClient(): TransactionalClient

    fun <T> getBasicServerlessFunctionInterface(handlerClass: Class<T>): T

}
