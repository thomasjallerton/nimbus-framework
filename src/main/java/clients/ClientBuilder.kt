package clients

import clients.document.DocumentStoreClient
import clients.document.DocumentStoreClientDynamo
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
import testing.LocalNimbusDeployment
import java.sql.Connection

object ClientBuilder {

    @JvmStatic
    fun <K, V> getKeyValueStoreClient(key: Class<K>, value: Class<V>): KeyValueStoreClient<K, V> {
        return if (LocalNimbusDeployment.isLocalDeployment) {
            KeyValueStoreClientLocal(key, value)
        } else {
            KeyValueStoreClientDynamo(key, value)
        }
    }

    @JvmStatic
    fun <T> getDocumentStoreClient(document: Class<T>): DocumentStoreClient<T> {
        return if (LocalNimbusDeployment.isLocalDeployment) {
            DocumentStoreClientDynamo(document)
        } else {
            DocumentStoreClientDynamo(document)
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
}
