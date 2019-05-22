package com.nimbusframework.nimbuscore.clients

import com.nimbusframework.nimbuscore.clients.document.DocumentStoreClient
import com.nimbusframework.nimbuscore.clients.document.DocumentStoreClientDynamo
import com.nimbusframework.nimbuscore.clients.document.DocumentStoreClientLocal
import com.nimbusframework.nimbuscore.clients.document.EmptyDocumentStoreClient
import com.nimbusframework.nimbuscore.clients.file.EmptyFileStorageClient
import com.nimbusframework.nimbuscore.clients.file.FileStorageClient
import com.nimbusframework.nimbuscore.clients.file.FileStorageClientLocal
import com.nimbusframework.nimbuscore.clients.file.FileStorageClientS3
import com.nimbusframework.nimbuscore.clients.function.*
import com.nimbusframework.nimbuscore.clients.keyvalue.EmptyKeyValueStoreClient
import com.nimbusframework.nimbuscore.clients.keyvalue.KeyValueStoreClient
import com.nimbusframework.nimbuscore.clients.keyvalue.KeyValueStoreClientDynamo
import com.nimbusframework.nimbuscore.clients.keyvalue.KeyValueStoreClientLocal
import com.nimbusframework.nimbuscore.clients.notification.EmptyNotificationClient
import com.nimbusframework.nimbuscore.clients.notification.NotificationClient
import com.nimbusframework.nimbuscore.clients.notification.NotificationClientLocal
import com.nimbusframework.nimbuscore.clients.notification.NotificationClientSNS
import com.nimbusframework.nimbuscore.clients.queue.EmptyQueueClient
import com.nimbusframework.nimbuscore.clients.queue.QueueClient
import com.nimbusframework.nimbuscore.clients.queue.QueueClientLocal
import com.nimbusframework.nimbuscore.clients.queue.QueueClientSQS
import com.nimbusframework.nimbuscore.clients.rdbms.DatabaseClient
import com.nimbusframework.nimbuscore.clients.rdbms.DatabaseClientLocal
import com.nimbusframework.nimbuscore.clients.rdbms.DatabaseClientRds
import com.nimbusframework.nimbuscore.clients.rdbms.EmptyDatabaseClient
import com.nimbusframework.nimbuscore.clients.websocket.EmptyServerlessFunctionWebSocketClient
import com.nimbusframework.nimbuscore.clients.websocket.ServerlessFunctionWebSocketClient
import com.nimbusframework.nimbuscore.clients.websocket.ServerlessFunctionWebSocketClientApiGateway
import com.nimbusframework.nimbuscore.clients.websocket.ServerlessFunctionWebsocketClientLocal

//Need to ensure that the only dependencies are clients, for the assumption made in the assembly

object ClientBuilder {

    var isLocalDeployment = false

    @JvmStatic
    fun <K, V> getKeyValueStoreClient(key: Class<K>, value: Class<V>): KeyValueStoreClient<K, V> {
        return if (isLocalDeployment) {
            KeyValueStoreClientLocal(value)
        } else {
            try {
                KeyValueStoreClientDynamo(key, value, getStage())
            } catch (e: ClassNotFoundException) {
                EmptyKeyValueStoreClient<K, V>()
            } catch (e: NoClassDefFoundError) {
                EmptyKeyValueStoreClient<K, V>()
            }
        }
    }

    @JvmStatic
    fun <T> getDocumentStoreClient(document: Class<T>): DocumentStoreClient<T> {
        return if (isLocalDeployment) {
            DocumentStoreClientLocal(document)
        } else {
            try {
                DocumentStoreClientDynamo(document, getStage())
            } catch (e: ClassNotFoundException) {
                EmptyDocumentStoreClient<T>()
            } catch (e: NoClassDefFoundError) {
                EmptyDocumentStoreClient<T>()
            }
        }
    }

    @JvmStatic
    fun getQueueClient(id: String): QueueClient {
        return if (isLocalDeployment) {
            QueueClientLocal(id)
        } else {
            try {
                QueueClientSQS(id)
            } catch (e: ClassNotFoundException) {
                EmptyQueueClient()
            } catch (e: NoClassDefFoundError) {
                EmptyQueueClient()
            }
        }
    }

    @JvmStatic
    fun <T> getDatabaseClient(databaseObject: Class<T>): DatabaseClient {
        return if (isLocalDeployment) {
            DatabaseClientLocal(databaseObject)
        } else {
            try {
                DatabaseClientRds(databaseObject)
            } catch (e: ClassNotFoundException) {
                EmptyDatabaseClient()
            } catch (e: NoClassDefFoundError) {
                EmptyDatabaseClient()
            }
        }
    }

    @JvmStatic
    fun <T> getEnvironmentVariableClient(): EnvironmentVariableClient {
        return if (isLocalDeployment) {
            EnvironmentVariableClientLocal()
        } else {
            try {
                EnvironmentVariableClientLambda()
            } catch (e: ClassNotFoundException) {
                EmptyEnvironmentVariableClient()
            } catch (e: NoClassDefFoundError) {
                EmptyEnvironmentVariableClient()
            }
        }
    }


    @JvmStatic
    fun getNotificationClient(topic: String): NotificationClient {
        return if (isLocalDeployment) {
            NotificationClientLocal(topic)
        } else {
            try {
                NotificationClientSNS(topic)
            } catch (e: ClassNotFoundException) {
                EmptyNotificationClient()
            } catch (e: NoClassDefFoundError) {
                EmptyNotificationClient()
            }
        }
    }

    @JvmStatic
    fun getBasicServerlessFunctionClient(): BasicServerlessFunctionClient {
        return if (isLocalDeployment) {
            BasicServerlessFunctionClientLocal()
        } else {
            try {
                BasicServerlessFunctionClientLambda()
            } catch (e: ClassNotFoundException) {
                EmptyBasicServerlessFunctionClient()
            } catch (e: NoClassDefFoundError) {
                EmptyBasicServerlessFunctionClient()
            }
        }
    }

    @JvmStatic
    fun getFileStorageClient(bucketName: String): FileStorageClient {
        return if (isLocalDeployment) {
            FileStorageClientLocal(bucketName)
        } else {
            try {
                FileStorageClientS3(bucketName + getStage())
            } catch (e: ClassNotFoundException) {
                EmptyFileStorageClient()
            } catch (e: NoClassDefFoundError) {
                EmptyFileStorageClient()
            }
        }
    }

    @JvmStatic
    fun getServerlessFunctionWebSocketClient(): ServerlessFunctionWebSocketClient {
        return if (isLocalDeployment) {
            ServerlessFunctionWebsocketClientLocal()
        } else {
            try {
                ServerlessFunctionWebSocketClientApiGateway()
            } catch (e: ClassNotFoundException) {
                EmptyServerlessFunctionWebSocketClient()
            } catch (e: NoClassDefFoundError) {
                EmptyServerlessFunctionWebSocketClient()
            }
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
