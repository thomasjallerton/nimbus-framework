package com.nimbusframework.nimbuslocal

import com.nimbusframework.nimbuscore.clients.ClientBuilder
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
import com.nimbusframework.nimbuslocal.exampleHandlers.ExampleBasicFunctionHandler
import com.nimbusframework.nimbuslocal.exampleModels.*
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.mockk

class NimbusMockInjectorTest : StringSpec({

    "correct queue client" {
        val client: QueueClient = mockk()
        NimbusMockInjector()
            .withQueueClient(Queue::class.java, client)
            .inject()
        ClientBuilder.getQueueClient(Queue::class.java) shouldBe client
    }

    "correct basic function client" {
        val client: BasicServerlessFunctionClient = mockk()
        NimbusMockInjector()
            .withBasicFunctionClient(ExampleBasicFunctionHandler::class.java, "method", client)
            .inject()
        ClientBuilder.getBasicServerlessFunctionClient(ExampleBasicFunctionHandler::class.java, "method") shouldBe client
    }

    "correct basic function interface client" {
        val client: ExampleBasicFunctionHandler = mockk()
        NimbusMockInjector()
            .withBasicFunctionInterface(ExampleBasicFunctionHandler::class.java, client)
            .inject()
        ClientBuilder.getBasicServerlessFunctionInterface(ExampleBasicFunctionHandler::class.java) shouldBe client
    }

    "correct default function interface client" {
        NimbusMockInjector()
            .withBasicFunctionInterface(ExampleBasicFunctionHandler::class.java)
            .inject()
        ClientBuilder.getBasicServerlessFunctionInterface(ExampleBasicFunctionHandler::class.java) shouldNotBe null
    }

    "correct key value store client" {
        val client: KeyValueStoreClient<String, KeyValue> = mockk()
        NimbusMockInjector()
            .withKeyValueStoreClient(KeyValue::class.java, client)
            .inject()
        ClientBuilder.getKeyValueStoreClient(String::class.java, KeyValue::class.java) shouldBe client
    }

    "correct document store client" {
        val client: DocumentStoreClient<Document> = mockk()
        NimbusMockInjector()
            .withDocumentStoreClient(Document::class.java, client)
            .inject()
        ClientBuilder.getDocumentStoreClient(Document::class.java) shouldBe client
    }

    "correct file storage store client" {
        val client: FileStorageClient = mockk()
        NimbusMockInjector()
            .withFileStorageClient(Bucket::class.java, client)
            .inject()
        ClientBuilder.getFileStorageClient(Bucket::class.java) shouldBe client
    }

    "correct notification client" {
        val client: NotificationClient = mockk()
        NimbusMockInjector()
            .withNotificationClient(NotificationTopic::class.java, client)
            .inject()
        ClientBuilder.getNotificationClient(NotificationTopic::class.java) shouldBe client
    }

    "correct database client" {
        val client: DatabaseClient = mockk()
        NimbusMockInjector()
            .withDatabaseClient(Database::class.java, client)
            .inject()
        ClientBuilder.getDatabaseClient(Database::class.java) shouldBe client
    }

    "correct transactional client" {
        val client: TransactionalClient = mockk()
        NimbusMockInjector()
            .withTransactionalClient(client)
            .inject()
        ClientBuilder.getTransactionalClient() shouldBe client
    }

    "correct websocket client" {
        val client: ServerlessFunctionWebSocketClient = mockk()
        NimbusMockInjector()
            .withWebSocketClient(client)
            .inject()
        ClientBuilder.getServerlessFunctionWebSocketClient() shouldBe client
    }

    "correct environment variable client" {
        val client: EnvironmentVariableClient = mockk()
        NimbusMockInjector()
            .withEnvironmentVariableClient(client)
            .inject()
        ClientBuilder.getEnvironmentVariableClient() shouldBe client
    }

})
