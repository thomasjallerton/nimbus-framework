package com.nimbusframework.nimbusaws.clients

import com.nimbusframework.nimbusaws.annotation.annotations.cognito.ExistingCognitoUserPool
import com.nimbusframework.nimbusaws.annotation.annotations.database.ParsedDatabaseConfig
import com.nimbusframework.nimbusaws.annotation.annotations.database.RdsDatabase
import com.nimbusframework.nimbusaws.annotation.annotations.document.DynamoDbDocumentStore
import com.nimbusframework.nimbusaws.annotation.annotations.keyvalue.DynamoDbKeyValueStore
import com.nimbusframework.nimbusaws.annotation.annotations.parsed.ParsedNotificationTopic
import com.nimbusframework.nimbusaws.annotation.annotations.parsed.ParsedQueueDefinition
import com.nimbusframework.nimbusaws.clients.cognito.AwsCognitoClient
import com.nimbusframework.nimbusaws.clients.cognito.CognitoClient
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
import com.nimbusframework.nimbusaws.clients.secretmanager.AwsSecretManagerClient
import com.nimbusframework.nimbusaws.clients.secretmanager.SecretManagerClient
import com.nimbusframework.nimbusaws.clients.websocket.ServerlessFunctionWebSocketClientApiGateway
import com.nimbusframework.nimbuscore.annotations.AnnotationHelper.getAnnotationForStage
import com.nimbusframework.nimbuscore.annotations.database.RelationalDatabaseDefinition
import com.nimbusframework.nimbuscore.annotations.document.DocumentStoreDefinition
import com.nimbusframework.nimbuscore.annotations.file.FileStorageBucketDefinition
import com.nimbusframework.nimbuscore.annotations.keyvalue.KeyValueStoreDefinition
import com.nimbusframework.nimbuscore.annotations.notification.NotificationTopicDefinition
import com.nimbusframework.nimbuscore.annotations.queue.QueueDefinition
import com.nimbusframework.nimbuscore.clients.AnnotationForStageService
import com.nimbusframework.nimbuscore.clients.InternalClientBuilder
import com.nimbusframework.nimbuscore.clients.database.DatabaseClient
import com.nimbusframework.nimbuscore.clients.document.DocumentStoreAnnotationService
import com.nimbusframework.nimbuscore.clients.document.DocumentStoreClient
import com.nimbusframework.nimbuscore.clients.file.FileStorageClient
import com.nimbusframework.nimbuscore.clients.function.BasicServerlessFunctionClient
import com.nimbusframework.nimbuscore.clients.function.EnvironmentVariableClient
import com.nimbusframework.nimbuscore.clients.keyvalue.KeyValueStoreAnnotationService
import com.nimbusframework.nimbuscore.clients.keyvalue.KeyValueStoreClient
import com.nimbusframework.nimbuscore.clients.notification.NotificationClient
import com.nimbusframework.nimbuscore.clients.queue.QueueClient
import com.nimbusframework.nimbuscore.clients.store.TransactionalClient
import com.nimbusframework.nimbuscore.clients.websocket.ServerlessFunctionWebSocketClient
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider
import software.amazon.awssdk.core.SdkSystemSetting
import software.amazon.awssdk.http.SdkHttpClient
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.apigatewaymanagementapi.ApiGatewayManagementApiClient
import software.amazon.awssdk.services.apigatewaymanagementapi.ApiGatewayManagementApiClientBuilder
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.lambda.LambdaClient
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sqs.SqsClient

object AwsInternalClientBuilder : InternalClientBuilder, InternalAwsClientBuilder {

    private val internalEnvironmentVariableClient = InternalEnvironmentVariableClient(getEnvironmentVariableClient())
    private val annotationForStageService = AnnotationForStageService()

    override fun getTransactionalClient(): TransactionalClient {
        val transactionalClient = DynamoTransactionClient(createDynamoDbClient())
        return transactionalClient
    }

    override fun <T> getBasicServerlessFunctionInterface(handlerClass: Class<T>): T {
        return Class.forName(handlerClass.canonicalName + "Serverless").getDeclaredConstructor().newInstance() as T
    }

    override fun isLocal(): Boolean {
        return false
    }

    override fun <K, V> getKeyValueStoreClient(key: Class<K>, value: Class<V>, stage: String): KeyValueStoreClient<K, V> {
        val agnosticAnnotation = getAnnotationForStage(value, KeyValueStoreDefinition::class, stage) { it.stages }
        val specificAnnotation = getAnnotationForStage(value, DynamoDbKeyValueStore::class, stage) { it.stages }
        val keyValueStoreClient = if (agnosticAnnotation != null) {
            val tableName = KeyValueStoreAnnotationService.getTableName(value, stage)
            val keyNameAndType = KeyValueStoreAnnotationService.getKeyNameAndType(value, stage)
            KeyValueStoreClientDynamo(key, value, stage, keyNameAndType.first, tableName, keyNameAndType.second) { columnNameMap: Map<String, String> ->
                DynamoClient(
                    tableName,
                    value.canonicalName,
                    keyNameAndType.first,
                    columnNameMap,
                    createDynamoDbClient()
                )
            }
        } else if (specificAnnotation != null) {
            val tableName = DynamoDbKeyValueStoreAnnotationService.getTableName(value, stage)
            val keyNameAndType = DynamoDbKeyValueStoreAnnotationService.getKeyNameAndType(value, stage)
            KeyValueStoreClientDynamo(key, value, stage, keyNameAndType.first, tableName, keyNameAndType.second) { columnNameMap: Map<String, String> ->
                DynamoClient(
                    tableName,
                    value.canonicalName,
                    keyNameAndType.first,
                    columnNameMap,
                    createDynamoDbClient()
                )
            }
        } else {
            throw IllegalStateException("${value.simpleName} is not a known type of Key Value Store (KeyValueStore, DynamoDbKeyValueStore)")
        }
        return keyValueStoreClient
    }

    override fun <T> getDocumentStoreClient(document: Class<T>, stage: String): DocumentStoreClient<T> {
        val agnosticAnnotation = getAnnotationForStage(document, DocumentStoreDefinition::class, stage) { it.stages }
        val specificAnnotation = getAnnotationForStage(document, DynamoDbDocumentStore::class, stage) { it.stages }

        val documentStoreClient = if (agnosticAnnotation != null) {
            val tableName = DocumentStoreAnnotationService.getTableName(document, stage)
            DocumentStoreClientDynamo(document, tableName, stage) { columnNameMap: Map<String, String>, keyColumn: String ->
                DynamoClient(
                    tableName,
                    document.canonicalName,
                    keyColumn,
                    columnNameMap,
                    createDynamoDbClient()
                )
            }
        } else if (specificAnnotation != null) {
            val tableName = DynamoDbDocumentStoreAnnotationService.getTableName(document, stage)
            DocumentStoreClientDynamo(document, tableName, stage) { columnNameMap: Map<String, String>, keyColumn: String  ->
                DynamoClient(
                    tableName,
                    document.canonicalName,
                    keyColumn,
                    columnNameMap,
                    createDynamoDbClient()
                )
            }
        } else {
            throw IllegalStateException("${document.simpleName} is not a known type of Document Store (DocumentStore, DynamoDbDocumentStore)")
        }
        return documentStoreClient
    }

    override fun getQueueClient(queueClass: Class<*>, stage: String): QueueClient {
        val queueAnnotation = annotationForStageService.getAnnotation(queueClass, QueueDefinition::class.java, stage) { it.stages }
        val parsed = ParsedQueueDefinition(queueAnnotation)
        return QueueClientSQS(parsed, createSqsClient(), internalEnvironmentVariableClient)
    }

    override fun <T> getDatabaseClient(databaseObject: Class<T>, stage: String): DatabaseClient {
        val agnosticAnnotation = getAnnotationForStage(databaseObject, RelationalDatabaseDefinition::class, stage) { it.stages }
        if (agnosticAnnotation != null) {
            val parsedDatabaseConfig = ParsedDatabaseConfig.fromRelationDatabase(agnosticAnnotation)
            return DatabaseClientRds(parsedDatabaseConfig, internalEnvironmentVariableClient)
        }
        val specificAnnotation = getAnnotationForStage(databaseObject, RdsDatabase::class, stage) { it.stages }
            ?: throw IllegalStateException("${databaseObject.simpleName} is not a known type of Relational Database (RelationalDatabaseDefinition, RdsDatabase)")
        val parsedDatabaseConfig = ParsedDatabaseConfig.fromRdsDatabase(specificAnnotation)
        return DatabaseClientRds(parsedDatabaseConfig, internalEnvironmentVariableClient)
    }

    override fun getEnvironmentVariableClient(): EnvironmentVariableClient {
        return EnvironmentVariableClientLambda()
    }

    override fun getNotificationClient(topicClass: Class<*>, stage: String): NotificationClient {
        val topicAnnotation = annotationForStageService.getAnnotation(topicClass, NotificationTopicDefinition::class.java, stage) { it.stages }
        return NotificationClientSNS(ParsedNotificationTopic(topicAnnotation), createSnsClient(), internalEnvironmentVariableClient)
    }

    override fun getBasicServerlessFunctionClient(
        handlerClass: Class<*>,
        functionName: String
    ): BasicServerlessFunctionClient {
        return BasicServerlessFunctionClientLambda(handlerClass, functionName, createLambdaClient(), internalEnvironmentVariableClient)
    }

    override fun getFileStorageClient(bucketClass: Class<*>, stage: String): FileStorageClient {
        val annotation = getAnnotationForStage(bucketClass, FileStorageBucketDefinition::class, stage) { it.stages }
        if (annotation != null) {
            return FileStorageClientS3(annotation.bucketName, stage, createS3Client())
        } else {
            throw IllegalStateException("${bucketClass.simpleName} is not a known type of File Storage Bucket. (Probably not annotated with @FileStorageBucketDefinition)")
        }
    }

    override fun getCognitoClient(userPool: Class<*>, stage: String): CognitoClient {
        val annotation = getAnnotationForStage(userPool, ExistingCognitoUserPool::class, stage) { it.stages }
        if (annotation != null) {
            return AwsCognitoClient(annotation.userPoolId, createCognitoClient())
        } else {
            throw IllegalStateException("${userPool.simpleName} is not a known type of Cognito. (Probably not annotated with @ExistingCognitoUserPool)")
        }
    }

    override fun getSecretClient(): SecretManagerClient {
        return AwsSecretManagerClient(createSecretsManagerClient())
    }

    override fun getServerlessFunctionWebSocketClient(): ServerlessFunctionWebSocketClient {
        return ServerlessFunctionWebSocketClientApiGateway(createApiGatewayManagementApiClient())
    }

    private val environmentVariableCredentialsProvider: EnvironmentVariableCredentialsProvider by lazy {
        EnvironmentVariableCredentialsProvider.create()
    }

    private var internalRegion: Region? = null
    private val region: Region by lazy {
        internalRegion ?: Region.of(System.getenv(SdkSystemSetting.AWS_REGION.environmentVariable()))
    }

    // Exposed for testing
    internal fun setRegion(region: Region): AwsInternalClientBuilder {
        internalRegion = region
        return this
    }

    private val urlConnectionHttpClient: SdkHttpClient by lazy {
        UrlConnectionHttpClient.builder().build()
    }

    private fun createDynamoDbClient(): DynamoDbClient {
        return DynamoDbClient.builder()
            .credentialsProvider(environmentVariableCredentialsProvider)
            .region(region)
            .httpClient(urlConnectionHttpClient)
            .build()
    }

    private fun createSnsClient(): SnsClient {
        return SnsClient.builder()
            .credentialsProvider(environmentVariableCredentialsProvider)
            .region(region)
            .httpClient(urlConnectionHttpClient)
            .build()
    }

    private fun createSqsClient(): SqsClient {
        return SqsClient.builder()
            .credentialsProvider(environmentVariableCredentialsProvider)
            .region(region)
            .httpClient(urlConnectionHttpClient)
            .build()
    }

    private fun createS3Client(): S3Client {
        return S3Client.builder()
            .credentialsProvider(environmentVariableCredentialsProvider)
            .region(region)
            .httpClient(urlConnectionHttpClient)
            .build()
    }

    private fun createLambdaClient(): LambdaClient {
        return LambdaClient.builder()
            .credentialsProvider(environmentVariableCredentialsProvider)
            .region(region)
            .httpClient(urlConnectionHttpClient)
            .build()
    }

    private fun createCognitoClient(): CognitoIdentityProviderClient {
        return CognitoIdentityProviderClient.builder()
            .credentialsProvider(environmentVariableCredentialsProvider)
            .region(region)
            .httpClient(urlConnectionHttpClient)
            .build()
    }

    private fun createApiGatewayManagementApiClient(): ApiGatewayManagementApiClientBuilder {
        return ApiGatewayManagementApiClient.builder()
            .credentialsProvider(environmentVariableCredentialsProvider)
            .region(region)
            .httpClient(urlConnectionHttpClient)
    }

    private fun createSecretsManagerClient(): SecretsManagerClient {
        return SecretsManagerClient.builder()
            .credentialsProvider(environmentVariableCredentialsProvider)
            .region(region)
            .httpClient(urlConnectionHttpClient)
            .build()
    }

}
