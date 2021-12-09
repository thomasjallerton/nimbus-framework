package com.nimbusframework.nimbuslocal.deployment.services

import com.nimbusframework.nimbuslocal.deployment.basic.BasicFunction
import com.nimbusframework.nimbuslocal.deployment.afterdeployment.AfterDeploymentMethod
import com.nimbusframework.nimbuslocal.deployment.document.LocalDocumentStore
import com.nimbusframework.nimbuslocal.deployment.file.LocalFileStorage
import com.nimbusframework.nimbuslocal.deployment.function.FunctionEnvironment
import com.nimbusframework.nimbuslocal.deployment.function.FunctionIdentifier
import com.nimbusframework.nimbuslocal.deployment.function.ServerlessFunction
import com.nimbusframework.nimbuslocal.deployment.http.HttpMethodIdentifier
import com.nimbusframework.nimbuslocal.deployment.http.LocalHttpMethod
import com.nimbusframework.nimbuslocal.deployment.keyvalue.LocalKeyValueStore
import com.nimbusframework.nimbuslocal.deployment.notification.LocalNotificationTopic
import com.nimbusframework.nimbuslocal.deployment.queue.LocalQueue
import com.nimbusframework.nimbuslocal.deployment.webserver.LocalHttpServer
import com.nimbusframework.nimbuslocal.deployment.webserver.WebServerHandler
import com.nimbusframework.nimbuslocal.deployment.websocket.LocalWebsocketMethod
import com.nimbusframework.nimbuslocal.deployment.websocketserver.LocalWebSocketServer
import org.eclipse.jetty.websocket.api.Session
import java.util.*

class LocalResourceHolder() {
    val functions: MutableMap<FunctionIdentifier, ServerlessFunction> = mutableMapOf()

    val queues: MutableMap<String, LocalQueue> = mutableMapOf()
    val httpMethods: MutableMap<HttpMethodIdentifier, LocalHttpMethod> = mutableMapOf()
    val websocketMethods: MutableMap<String, LocalWebsocketMethod> = mutableMapOf()
    val basicMethods: MutableMap<FunctionIdentifier, BasicFunction> = mutableMapOf()
    val keyValueStores: MutableMap<Class<*>, LocalKeyValueStore<out Any, out Any>> = mutableMapOf()
    val documentStores: MutableMap<Class<*>, LocalDocumentStore<out Any>> = mutableMapOf()
    val fileStorage: MutableMap<String, LocalFileStorage> = mutableMapOf()
    val notificationTopics: MutableMap<String, LocalNotificationTopic> = mutableMapOf()
    val afterDeployments: Deque<AfterDeploymentMethod> = LinkedList()

    val functionEnvironments: MutableMap<FunctionIdentifier, FunctionEnvironment> = mutableMapOf()

    val webKeyValueStores: MutableMap<String, LocalKeyValueStore<out Any, out Any>> = mutableMapOf()
    val webDocumentStores: MutableMap<String, LocalDocumentStore<out Any>> = mutableMapOf()

    val httpServers: MutableMap<String, LocalHttpServer> = mutableMapOf()
    val webSocketSessions: MutableMap<String, Session> = mutableMapOf()
    val webSocketServer = LocalWebSocketServer(webSocketSessions)

}
