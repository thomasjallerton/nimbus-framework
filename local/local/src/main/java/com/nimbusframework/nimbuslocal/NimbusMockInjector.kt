package com.nimbusframework.nimbuslocal

import com.nimbusframework.nimbuscore.annotations.NimbusConstants
import com.nimbusframework.nimbuscore.clients.ClientBinder
import com.nimbusframework.nimbuscore.clients.database.DatabaseClient
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
import com.nimbusframework.nimbuslocal.clients.mock.MockClientBuilder
import com.nimbusframework.nimbuslocal.deployment.function.FunctionIdentifier

class NimbusMockInjector(
    private val stage: String
) {

    constructor(): this(NimbusConstants.stage)

    private val queueClients: MutableMap<String, QueueClient> = mutableMapOf()
    private val basicFunctionClients: MutableMap<FunctionIdentifier, BasicServerlessFunctionClient> = mutableMapOf()
    private val basicFunctionInterfaces: MutableMap<Class<*>, Any> = mutableMapOf()
    private val keyValueStoreClients: MutableMap<Class<*>, KeyValueStoreClient<out Any, out Any>> = mutableMapOf()
    private val documentStoreClients: MutableMap<Class<*>, DocumentStoreClient<out Any>> = mutableMapOf()
    private val fileStorageClients: MutableMap<String, FileStorageClient> = mutableMapOf()
    private val notificationTopicClients: MutableMap<String, NotificationClient> = mutableMapOf()
    private val databaseClients: MutableMap<Class<*>, DatabaseClient> = mutableMapOf()
    private var transactionalClient: TransactionalClient? = null
    private var webSocketClient: ServerlessFunctionWebSocketClient? = null
    private var environmentVariableClient: EnvironmentVariableClient? = null

    fun withQueueClient(queueClass: Class<*>, queueClient: QueueClient): NimbusMockInjector {
        queueClients[QueueIdAnnotationService.getQueueId(queueClass, stage)] = queueClient
        return this
    }

    fun withBasicFunctionClient(handlerClass: Class<*>, functionName: String, basicServerlessFunctionClient: BasicServerlessFunctionClient): NimbusMockInjector {
        basicFunctionClients[FunctionIdentifier(handlerClass, functionName)] = basicServerlessFunctionClient
        return this
    }

    fun <T: Any> withBasicFunctionInterface(handlerClass: Class<T>, basicFunctionInterface: T): NimbusMockInjector {
        basicFunctionInterfaces[handlerClass] = basicFunctionInterface
        return this
    }

    fun <T: Any> withBasicFunctionInterface(handlerClass: Class<T>): NimbusMockInjector {
        basicFunctionInterfaces[handlerClass] = handlerClass.getDeclaredConstructor().newInstance() as T
        return this
    }

    fun <K: Any, V: Any> withKeyValueStoreClient(value: Class<V>, keyValueStoreClient: KeyValueStoreClient<K, V>): NimbusMockInjector {
        keyValueStoreClients[value] = keyValueStoreClient
        return this
    }

    fun <T: Any> withDocumentStoreClient(document: Class<T>, documentStoreClient: DocumentStoreClient<T>): NimbusMockInjector {
        documentStoreClients[document] = documentStoreClient
        return this
    }

    fun withFileStorageClient(bucketClass: Class<*>, fileStorageClient: FileStorageClient): NimbusMockInjector {
        fileStorageClients[FileStorageBucketNameAnnotationService.getBucketName(bucketClass, stage)] = fileStorageClient
        return this
    }

    fun withNotificationClient(topicClass: Class<*>, notificationClient: NotificationClient): NimbusMockInjector {
        notificationTopicClients[NotificationTopicAnnotationService.getTopicName(topicClass, stage)] = notificationClient
        return this
    }

    fun <T: Any> withDatabaseClient(databaseObject: Class<T>, databaseClient: DatabaseClient): NimbusMockInjector {
        databaseClients[databaseObject] = databaseClient
        return this
    }

    fun withTransactionalClient(transactionalClient: TransactionalClient): NimbusMockInjector {
        this.transactionalClient = transactionalClient
        return this
    }

    fun withWebSocketClient(webSocketClient: ServerlessFunctionWebSocketClient): NimbusMockInjector {
        this.webSocketClient = webSocketClient
        return this
    }

    fun withEnvironmentVariableClient(environmentVariableClient: EnvironmentVariableClient): NimbusMockInjector {
        this.environmentVariableClient = environmentVariableClient
        return this
    }

    fun inject() {
        val clientBuilder = MockClientBuilder(
            queueClients,
            basicFunctionClients,
            basicFunctionInterfaces,
            keyValueStoreClients,
            documentStoreClients,
            fileStorageClients,
            notificationTopicClients,
            databaseClients,
            transactionalClient,
            webSocketClient,
            environmentVariableClient
        )
        ClientBinder.setInternalBuilder(clientBuilder)
    }

}