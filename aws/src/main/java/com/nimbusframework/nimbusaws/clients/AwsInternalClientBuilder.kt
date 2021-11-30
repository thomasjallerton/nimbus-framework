package com.nimbusframework.nimbusaws.clients

import com.nimbusframework.nimbusaws.annotation.annotations.document.DynamoDbDocumentStore
import com.nimbusframework.nimbusaws.annotation.annotations.keyvalue.DynamoDbKeyValueStore
import com.nimbusframework.nimbusaws.clients.document.DocumentStoreClientDynamo
import com.nimbusframework.nimbusaws.clients.document.DynamoDbDocumentStoreAnnotationService
import com.nimbusframework.nimbusaws.clients.dynamo.DynamoClient
import com.nimbusframework.nimbusaws.clients.dynamo.DynamoTransactionClient
import com.nimbusframework.nimbusaws.clients.file.FileStorageClientS3
import com.nimbusframework.nimbusaws.clients.function.BasicServerlessFunctionClientLambda
import com.nimbusframework.nimbusaws.clients.function.EnvironmentVariableClientLambda
import com.nimbusframework.nimbusaws.clients.keyvalue.DynamoDbKeyValueStoreAnnotationService
import com.nimbusframework.nimbusaws.clients.keyvalue.KeyValueStoreClientDynamo
import com.nimbusframework.nimbusaws.clients.notification.NotificationClientSNS
import com.nimbusframework.nimbusaws.clients.queue.QueueClientSQS
import com.nimbusframework.nimbusaws.clients.rdbms.DatabaseClientRds
import com.nimbusframework.nimbusaws.clients.websocket.ServerlessFunctionWebSocketClientApiGateway
import com.nimbusframework.nimbuscore.annotations.document.DocumentStoreDefinition
import com.nimbusframework.nimbuscore.annotations.file.FileStorageBucketDefinition
import com.nimbusframework.nimbuscore.annotations.keyvalue.KeyValueStoreDefinition
import com.nimbusframework.nimbuscore.clients.database.DatabaseClient
import com.nimbusframework.nimbuscore.clients.database.InternalClientBuilder
import com.nimbusframework.nimbuscore.clients.document.DocumentStoreAnnotationService
import com.nimbusframework.nimbuscore.clients.document.DocumentStoreClient
import com.nimbusframework.nimbuscore.clients.file.FileStorageClient
import com.nimbusframework.nimbuscore.clients.function.BasicServerlessFunctionClient
import com.nimbusframework.nimbuscore.clients.function.EnvironmentVariableClient
import com.nimbusframework.nimbuscore.clients.keyvalue.KeyValueStoreAnnotationService
import com.nimbusframework.nimbuscore.clients.keyvalue.KeyValueStoreClient
import com.nimbusframework.nimbuscore.clients.notification.NotificationClient
import com.nimbusframework.nimbuscore.clients.notification.NotificationTopicAnnotationService
import com.nimbusframework.nimbuscore.clients.queue.QueueClient
import com.nimbusframework.nimbuscore.clients.queue.QueueIdAnnotationService
import com.nimbusframework.nimbuscore.clients.store.TransactionalClient
import com.nimbusframework.nimbuscore.clients.websocket.ServerlessFunctionWebSocketClient
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sqs.SqsClient

object AwsInternalClientBuilder: InternalClientBuilder {

    override fun getTransactionalClient(): TransactionalClient {
        val transactionalClient = DynamoTransactionClient(createDynamoDbClient())
        return transactionalClient
    }

    override fun <T> getBasicServerlessFunctionInterface(handlerClass: Class<T>): T {
        return Class.forName(handlerClass.canonicalName + "Serverless").getDeclaredConstructor().newInstance() as T
    }

    override fun <K, V> getKeyValueStoreClient(key: Class<K>, value: Class<V>, stage: String): KeyValueStoreClient<K, V> {
        val keyValueStoreClient = when {
            value.isAnnotationPresent(KeyValueStoreDefinition::class.java) -> {
                val tableName = KeyValueStoreAnnotationService.getTableName(value, stage)
                val keyNameAndType = KeyValueStoreAnnotationService.getKeyNameAndType(value, stage)
                KeyValueStoreClientDynamo(key, value, stage, keyNameAndType.first, tableName, keyNameAndType.second) { columnNameMap: Map<String, String> ->
                    DynamoClient(
                        tableName,
                        value.canonicalName,
                        columnNameMap,
                        createDynamoDbClient()
                    )
                }
            }
            value.isAnnotationPresent(DynamoDbKeyValueStore::class.java) -> {
                val tableName = DynamoDbKeyValueStoreAnnotationService.getTableName(value, stage)
                val keyNameAndType = DynamoDbKeyValueStoreAnnotationService.getKeyNameAndType(value, stage)
                KeyValueStoreClientDynamo(key, value, stage, keyNameAndType.first, tableName, keyNameAndType.second) { columnNameMap: Map<String, String> ->
                    DynamoClient(
                        tableName,
                        value.canonicalName,
                        columnNameMap,
                        createDynamoDbClient()
                    )
                }
            }
            else -> throw IllegalStateException("${value.simpleName} is not a known type of Key Value Store (KeyValueStore, DynamoDbKeyValueStore)")
        }
        return keyValueStoreClient
    }

    override fun <T> getDocumentStoreClient(document: Class<T>, stage: String): DocumentStoreClient<T> {
        val documentStoreClient = when {
            document.isAnnotationPresent(DocumentStoreDefinition::class.java) -> {
                val tableName = DocumentStoreAnnotationService.getTableName(document, stage)
                DocumentStoreClientDynamo(document, tableName, stage) { columnNameMap: Map<String, String> ->
                    DynamoClient(
                        tableName,
                        document.canonicalName,
                        columnNameMap,
                        createDynamoDbClient()
                    )
                }
            }
            document.isAnnotationPresent(DynamoDbDocumentStore::class.java) -> {
                val tableName = DynamoDbDocumentStoreAnnotationService.getTableName(document, stage)
                DocumentStoreClientDynamo(document, tableName, stage) { columnNameMap: Map<String, String> ->
                    DynamoClient(
                        tableName,
                        document.canonicalName,
                        columnNameMap,
                        createDynamoDbClient()
                    )
                }
            }
            else -> throw IllegalStateException("${document.simpleName} is not a known type of Document Store (DocumentStore, DynamoDbDocumentStore)")
        }
        return documentStoreClient
    }

    override fun getQueueClient(queueClass: Class<*>, stage: String): QueueClient {
        val queueId = QueueIdAnnotationService.getQueueId(queueClass, stage)
        val queueClient = QueueClientSQS(queueId, createSqsClient(), getEnvironmentVariableClient())
        return queueClient
    }

    override fun <T> getDatabaseClient(databaseObject: Class<T>): DatabaseClient {
        return DatabaseClientRds(databaseObject, getEnvironmentVariableClient())
    }

    override fun getEnvironmentVariableClient(): EnvironmentVariableClient {
        return EnvironmentVariableClientLambda()
    }

    override fun getNotificationClient(topicClass: Class<*>, stage: String): NotificationClient {
        val topic = NotificationTopicAnnotationService.getTopicName(topicClass, stage)
        return NotificationClientSNS(topic, createSnsClient(), getEnvironmentVariableClient())
    }

    override fun getBasicServerlessFunctionClient(
        handlerClass: Class<*>,
        functionName: String
    ): BasicServerlessFunctionClient {
        return BasicServerlessFunctionClientLambda(handlerClass, functionName)
    }

    override fun getFileStorageClient(bucketClass: Class<*>, stage: String): FileStorageClient {
        if (bucketClass.isAnnotationPresent(FileStorageBucketDefinition::class.java)) {
            val bucketName = bucketClass.getAnnotation(FileStorageBucketDefinition::class.java).bucketName
            return FileStorageClientS3(bucketName, stage, createS3Client())
        } else {
            throw IllegalStateException("${bucketClass.simpleName} is not a known type of File Storage Bucket. (Probably not annotated with @FileStorageBucketDefinition)")
        }
    }

    override fun getServerlessFunctionWebSocketClient(): ServerlessFunctionWebSocketClient {
        val webSocketClientApi = ServerlessFunctionWebSocketClientApiGateway()
        return webSocketClientApi
    }

    private fun createDynamoDbClient(): DynamoDbClient {
        return DynamoDbClient.builder()
            .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
            .httpClient(UrlConnectionHttpClient.builder().build())
            .build()
    }

    private fun createSnsClient(): SnsClient {
        return SnsClient.builder()
            .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
            .httpClient(UrlConnectionHttpClient.builder().build())
            .build()
    }

    private fun createSqsClient(): SqsClient {
        return SqsClient.builder()
            .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
            .httpClient(UrlConnectionHttpClient.builder().build())
            .build()
    }

    private fun createS3Client(): S3Client {
        return S3Client.builder()
            .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
            .httpClient(UrlConnectionHttpClient.builder().build())
            .build()
    }


}
