package com.nimbusframework.nimbuslocal.clients.mock

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
import com.nimbusframework.nimbuslocal.deployment.function.FunctionIdentifier
import com.nimbusframework.nimbuslocal.exampleHandlers.ExampleBasicFunctionHandler
import com.nimbusframework.nimbuslocal.exampleModels.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.throwables.shouldThrowMessage
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk

class MockClientBuilderTest : StringSpec({

    "can inject queue client" {
        val queueClient = mockk<QueueClient>()
        val underTest = MockClientBuilder(queueClients = mapOf(Pair("QueueId", queueClient)))
        underTest.getQueueClient(Queue::class.java, "") shouldBe queueClient
    }

    "can inject basic function client" {
        val client = mockk<BasicServerlessFunctionClient>()
        val clazz = ExampleBasicFunctionHandler::class.java
        val functionIdentifier = FunctionIdentifier(clazz, "methodName")
        val underTest = MockClientBuilder(basicFunctionClients = mapOf(Pair(functionIdentifier, client)))
        underTest.getBasicServerlessFunctionClient(clazz, "methodName") shouldBe client
    }

    "can inject basic function interface" {
        val client = mockk<ExampleBasicFunctionHandler>()
        val clazz = ExampleBasicFunctionHandler::class.java
        val underTest = MockClientBuilder(basicFunctionInterfaces = mapOf(Pair(clazz, client)))
        underTest.getBasicServerlessFunctionInterface(clazz) shouldBe client
    }

    "can inject key value store client" {
        val client = mockk<KeyValueStoreClient<out Any, out Any>>()
        val underTest = MockClientBuilder(keyValueStoreClients = mapOf(Pair(KeyValue::class.java, client)))
        underTest.getKeyValueStoreClient(String::class.java, KeyValue::class.java, "") shouldBe client
    }

    "can inject document store client" {
        val client = mockk<DocumentStoreClient<out Any>>()
        val underTest = MockClientBuilder(documentStoreClients = mapOf(Pair(Document::class.java, client)))
        underTest.getDocumentStoreClient(Document::class.java, "") shouldBe client
    }

    "can inject file storage client" {
        val client = mockk<FileStorageClient>()
        val underTest = MockClientBuilder(fileStorageClients = mapOf(Pair("Test", client)))
        underTest.getFileStorageClient(Bucket::class.java, "") shouldBe client
    }

    "can inject notification client" {
        val client = mockk<NotificationClient>()
        val underTest = MockClientBuilder(notificationTopicClients = mapOf(Pair("Topic", client)))
        underTest.getNotificationClient(NotificationTopic::class.java, "") shouldBe client
    }

    "can inject database client" {
        val client = mockk<DatabaseClient>()
        val underTest = MockClientBuilder(databaseClients = mapOf(Pair(Database::class.java, client)))
        underTest.getDatabaseClient(Database::class.java) shouldBe client
    }

    "can inject transactional client" {
        val client = mockk<TransactionalClient>()
        val underTest = MockClientBuilder(transactionalClient = client)
        underTest.getTransactionalClient() shouldBe client
    }

    "can inject websocket client" {
        val client = mockk<ServerlessFunctionWebSocketClient>()
        val underTest = MockClientBuilder(webSocketClient = client)
        underTest.getServerlessFunctionWebSocketClient() shouldBe client
    }

    "can inject environment variable client" {
        val client = mockk<EnvironmentVariableClient>()
        val underTest = MockClientBuilder(environmentVariableClient = client)
        underTest.getEnvironmentVariableClient() shouldBe client
    }

    "correct missing basic serverless function mock error" {
        val underTest = MockClientBuilder()
        shouldThrowMessage("Missing mock for BasicServerlessFunctionClient") {
            underTest.getBasicServerlessFunctionClient(ExampleBasicFunctionHandler::class.java, "methodName")
        }
    }

    "correct missing database mock error" {
        val underTest = MockClientBuilder()
        shouldThrowMessage("Missing mock for DatabaseClient") {
            underTest.getDatabaseClient(Database::class.java)
        }
    }

    "correct missing document store mock error" {
        val underTest = MockClientBuilder()
        shouldThrowMessage("Missing mock for DocumentStoreClient") {
            underTest.getDocumentStoreClient(Document::class.java, "")
        }
    }

    "correct missing transactional client mock error" {
        val underTest = MockClientBuilder()
        shouldThrowMessage("Missing mock for TransactionalClient") {
            underTest.getTransactionalClient()
        }
    }

    "correct missing environmental client mock error" {
        val underTest = MockClientBuilder()
        shouldThrowMessage("Missing mock for EnvironmentVariableClient") {
            underTest.getEnvironmentVariableClient()
        }
    }

    "correct missing file storage client mock error" {
        val underTest = MockClientBuilder()
        shouldThrowMessage("Missing mock for FileStorageClient") {
            underTest.getFileStorageClient(Bucket::class.java, "")
        }
    }

    "correct missing key value client mock error" {
        val underTest = MockClientBuilder()
        shouldThrowMessage("Missing mock for KeyValueStoreClient") {
            underTest.getKeyValueStoreClient(String::class.java, KeyValue::class.java, "")
        }
    }

    "correct missing notification client mock error" {
        val underTest = MockClientBuilder()
        shouldThrowMessage("Missing mock for NotificationClient") {
            underTest.getNotificationClient(NotificationTopic::class.java, "")
        }
    }

    "correct missing queue client mock error" {
        val underTest = MockClientBuilder()
        shouldThrowMessage("Missing mock for QueueClient") {
            underTest.getQueueClient(Queue::class.java, "")
        }
    }

    "correct missing websocket client mock error" {
        val underTest = MockClientBuilder()
        shouldThrowMessage("Missing mock for ServerlessFunctionWebSocketClient") {
            underTest.getServerlessFunctionWebSocketClient()
        }
    }

    "correct missing basic function interface client mock error" {
        val underTest = MockClientBuilder()
        shouldThrowMessage("Missing mock for function interface ExampleBasicFunctionHandler") {
            underTest.getBasicServerlessFunctionInterface(ExampleBasicFunctionHandler::class.java)
        }
    }
})
