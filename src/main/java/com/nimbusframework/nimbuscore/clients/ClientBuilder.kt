package com.nimbusframework.nimbuscore.clients

import com.nimbusframework.nimbuscore.clients.document.DocumentStoreClient
import com.nimbusframework.nimbuscore.clients.document.DocumentStoreClientDynamo
import com.nimbusframework.nimbuscore.clients.document.DocumentStoreClientLocal
import com.nimbusframework.nimbuscore.clients.file.FileStorageClient
import com.nimbusframework.nimbuscore.clients.file.FileStorageClientLocal
import com.nimbusframework.nimbuscore.clients.file.FileStorageClientS3
import com.nimbusframework.nimbuscore.clients.function.BasicServerlessFunctionClient
import com.nimbusframework.nimbuscore.clients.function.BasicServerlessFunctionClientLambda
import com.nimbusframework.nimbuscore.clients.function.BasicServerlessFunctionClientLocal
import com.nimbusframework.nimbuscore.clients.keyvalue.KeyValueStoreClient
import com.nimbusframework.nimbuscore.clients.keyvalue.KeyValueStoreClientDynamo
import com.nimbusframework.nimbuscore.clients.keyvalue.KeyValueStoreClientLocal
import com.nimbusframework.nimbuscore.clients.notification.NotificationClient
import com.nimbusframework.nimbuscore.clients.notification.NotificationClientLocal
import com.nimbusframework.nimbuscore.clients.notification.NotificationClientSNS
import com.nimbusframework.nimbuscore.clients.queue.QueueClient
import com.nimbusframework.nimbuscore.clients.queue.QueueClientSQS
import com.nimbusframework.nimbuscore.clients.queue.QueueClientLocal
import com.nimbusframework.nimbuscore.clients.rdbms.DatabaseClient
import com.nimbusframework.nimbuscore.clients.rdbms.DatabaseClientLocal
import com.nimbusframework.nimbuscore.clients.rdbms.DatabaseClientRds
import com.nimbusframework.nimbuscore.clients.websocket.ServerlessFunctionWebSocketClient
import com.nimbusframework.nimbuscore.clients.websocket.ServerlessFunctionWebSocketClientApiGateway
import com.nimbusframework.nimbuscore.clients.websocket.ServerlessFunctionWebsocketClientLocal
import com.nimbusframework.nimbuscore.testing.LocalNimbusDeployment

object ClientBuilder {

    @JvmStatic
    fun <K, V> getKeyValueStoreClient(key: Class<K>, value: Class<V>): KeyValueStoreClient<K, V> {

        return if (LocalNimbusDeployment.isLocalDeployment) {
            KeyValueStoreClientLocal(key, value, LocalNimbusDeployment.stage)
        } else {
            KeyValueStoreClientDynamo(key, value, getStage())
        }
    }

    @JvmStatic
    fun <T> getDocumentStoreClient(document: Class<T>): DocumentStoreClient<T> {
        return if (LocalNimbusDeployment.isLocalDeployment) {
            DocumentStoreClientLocal(document, LocalNimbusDeployment.stage)
        } else {
            DocumentStoreClientDynamo(document, getStage())
        }
    }

    @JvmStatic
    fun getQueueClient(id: String): QueueClient {
        return if (LocalNimbusDeployment.isLocalDeployment) {
            QueueClientLocal(id)
        } else {
            QueueClientSQS(id)
        }
    }

    @JvmStatic
    fun <T> getDatabaseClient(databaseObject: Class<T>): DatabaseClient {
        return if (LocalNimbusDeployment.isLocalDeployment) {
            DatabaseClientLocal(databaseObject)
        } else {
            DatabaseClientRds(databaseObject)
        }
    }

    @JvmStatic
    fun getNotificationClient(topic: String): NotificationClient {
        return if (LocalNimbusDeployment.isLocalDeployment) {
            NotificationClientLocal(topic)
        } else {
            NotificationClientSNS(topic)
        }
    }

    @JvmStatic
    fun getBasicServerlessFunctionClient(): BasicServerlessFunctionClient {
        return if (LocalNimbusDeployment.isLocalDeployment) {
            BasicServerlessFunctionClientLocal()
        } else {
            BasicServerlessFunctionClientLambda()
        }
    }

    @JvmStatic
    fun getFileStorageClient(bucketName: String): FileStorageClient {
        return if (LocalNimbusDeployment.isLocalDeployment) {
            FileStorageClientLocal(bucketName)
        } else {
            FileStorageClientS3(bucketName + getStage())
        }
    }

    @JvmStatic
    fun getServerlessFunctionWebSocketClient(): ServerlessFunctionWebSocketClient {
        return if (LocalNimbusDeployment.isLocalDeployment) {
            ServerlessFunctionWebsocketClientLocal()
        } else {
            ServerlessFunctionWebSocketClientApiGateway()
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
