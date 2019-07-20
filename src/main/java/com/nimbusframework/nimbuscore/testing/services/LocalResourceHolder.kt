package com.nimbusframework.nimbuscore.testing.services

import com.nimbusframework.nimbuscore.testing.ServerlessMethod
import com.nimbusframework.nimbuscore.testing.basic.BasicFunction
import com.nimbusframework.nimbuscore.testing.deployment.AfterDeploymentMethod
import com.nimbusframework.nimbuscore.testing.document.LocalDocumentStore
import com.nimbusframework.nimbuscore.testing.file.LocalFileStorage
import com.nimbusframework.nimbuscore.testing.function.FunctionEnvironment
import com.nimbusframework.nimbuscore.testing.function.FunctionIdentifier
import com.nimbusframework.nimbuscore.testing.http.HttpMethodIdentifier
import com.nimbusframework.nimbuscore.testing.http.LocalHttpMethod
import com.nimbusframework.nimbuscore.testing.keyvalue.LocalKeyValueStore
import com.nimbusframework.nimbuscore.testing.notification.LocalNotificationTopic
import com.nimbusframework.nimbuscore.testing.queue.LocalQueue
import com.nimbusframework.nimbuscore.testing.webserver.LocalHttpServer
import com.nimbusframework.nimbuscore.testing.webserver.WebserverHandler
import com.nimbusframework.nimbuscore.testing.websocket.LocalWebsocketMethod
import com.nimbusframework.nimbuscore.testing.websocketserver.LocalWebSocketServer
import org.eclipse.jetty.websocket.api.Session
import java.util.*

class LocalResourceHolder(stage: String) {
    val methods: MutableMap<FunctionIdentifier, ServerlessMethod> = mutableMapOf()

    val queues: MutableMap<String, LocalQueue> = mutableMapOf()
    val httpMethods: MutableMap<HttpMethodIdentifier, LocalHttpMethod> = mutableMapOf()
    val websocketMethods: MutableMap<String, LocalWebsocketMethod> = mutableMapOf()
    val basicMethods: MutableMap<FunctionIdentifier, BasicFunction> = mutableMapOf()
    val keyValueStores: MutableMap<String, LocalKeyValueStore<out Any, out Any>> = mutableMapOf()
    val documentStores: MutableMap<String, LocalDocumentStore<out Any>> = mutableMapOf()
    val fileStorage: MutableMap<String, LocalFileStorage> = mutableMapOf()
    val notificationTopics: MutableMap<String, LocalNotificationTopic> = mutableMapOf()
    val afterDeployments: Deque<AfterDeploymentMethod> = LinkedList()

    val functionEnvironments: MutableMap<FunctionIdentifier, FunctionEnvironment> = mutableMapOf()

    val httpServers: MutableMap<String, WebserverHandler> = mutableMapOf()
    val webSocketSessions: MutableMap<String, Session> = mutableMapOf()
    val webSocketServer = LocalWebSocketServer(webSocketSessions)
    val httpServer = LocalHttpServer(stage)

}