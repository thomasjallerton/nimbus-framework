package com.nimbusframework.nimbuscore.persisted

enum class ClientType {
    DocumentStore,
    KeyValueStore,
    FileStorage,
    EnvironmentVariable,
    BasicFunction,
    Notification,
    Queue,
    Database,
    WebSocket;

    fun toClassPaths(): List<String> {
        return when (this) {
            DocumentStore -> listOf(
                    "com.nimbusframework.nimbuscore.clients.document.DocumentStoreClient",
                    "com.nimbusframework.nimbuscore.clients.document.DocumentStoreClientDynamo"
            )
            KeyValueStore -> listOf(
                    "com.nimbusframework.nimbuscore.clients.keyvalue.KeyValueStoreClient",
                    "com.nimbusframework.nimbuscore.clients.keyvalue.KeyValueStoreClientDynamo"
            )
            FileStorage -> listOf(
                    "com.nimbusframework.nimbuscore.clients.file.FileStorageClient",
                    "com.nimbusframework.nimbuscore.clients.file.FileStorageClientS3"
            )
            EnvironmentVariable -> listOf(
                    "com.nimbusframework.nimbuscore.clients.function.EnvironmentVariableClient",
                    "com.nimbusframework.nimbuscore.clients.function.EnvironmentVariableClientLambda"
            )
            BasicFunction -> listOf(
                    "com.nimbusframework.nimbuscore.clients.function.BasicServerlessFunctionClient",
                    "com.nimbusframework.nimbuscore.clients.function.BasicServerlessFunctionClientLambda"
            )
            Notification -> listOf(
                    "com.nimbusframework.nimbuscore.clients.notification.NotificationClient",
                    "com.nimbusframework.nimbuscore.clients.notification.NotificationClientSNS"
            )
            Queue -> listOf(
                    "com.nimbusframework.nimbuscore.clients.queue.QueueClient",
                    "com.nimbusframework.nimbuscore.clients.queue.QueueClientSQS"
            )
            Database -> listOf(
                    "com.nimbusframework.nimbuscore.clients.rdbms.DatabaseClient",
                    "com.nimbusframework.nimbuscore.clients.rdbms.DatabaseClientRds"
            )
            WebSocket -> listOf(
                    "com.nimbusframework.nimbuscore.clients.websocket.ServerlessFunctionWebSocketClient",
                    "com.nimbusframework.nimbuscore.clients.websocket.ServerlessFunctionWebSocketClientApiGateway"
            )
        }
    }
}