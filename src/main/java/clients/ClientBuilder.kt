package clients

import clients.document.DocumentStoreClient
import clients.document.DocumentStoreClientDynamo
import clients.document.DocumentStoreClientLocal
import clients.file.FileStorageClient
import clients.file.FileStorageClientLocal
import clients.file.FileStorageClientS3
import clients.function.BasicServerlessFunctionClient
import clients.function.BasicServerlessFunctionClientLambda
import clients.function.BasicServerlessFunctionClientLocal
import clients.keyvalue.KeyValueStoreClient
import clients.keyvalue.KeyValueStoreClientDynamo
import clients.keyvalue.KeyValueStoreClientLocal
import clients.notification.NotificationClient
import clients.notification.NotificationClientLocal
import clients.notification.NotificationClientSNS
import clients.queue.QueueClient
import clients.queue.QueueClientDynamo
import clients.queue.QueueClientLocal
import clients.rdbms.DatabaseClient
import clients.rdbms.DatabaseClientLocal
import clients.rdbms.DatabaseClientRds
import clients.websocket.ServerlessFunctionWebSocketClient
import clients.websocket.ServerlessFunctionWebSocketClientApiGateway
import clients.websocket.ServerlessFunctionWebsocketClientLocal
import testing.LocalNimbusDeployment
import java.sql.Connection

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
            QueueClientDynamo(id)
        }
    }

    @JvmStatic
    fun <T> getRelationalDatabase(databaseObject: Class<T>): DatabaseClient {
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
