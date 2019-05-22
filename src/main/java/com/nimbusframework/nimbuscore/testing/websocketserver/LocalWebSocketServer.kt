package com.nimbusframework.nimbuscore.testing.websocketserver

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import org.eclipse.jetty.websocket.api.Session
import com.nimbusframework.nimbuscore.testing.websocket.LocalWebsocketMethod

class LocalWebSocketServer(
        private val sessions: MutableMap<String, Session>
) {

    private var server: Server? = null
    private var connectMethod: LocalWebsocketMethod? = null
    private var disconnectMethod: LocalWebsocketMethod? = null
    private var defaultMethod: LocalWebsocketMethod? = null
    private val topics: MutableMap<String, LocalWebsocketMethod> = mutableMapOf()

    fun setup(port: Int) {
        val newServer = Server()
        val connector = ServerConnector(newServer)

        connector.port = port
        newServer.addConnector(connector)

        val handler = ServletContextHandler(ServletContextHandler.SESSIONS)
        handler.contextPath = "/"
        newServer.handler = handler

        val servletHolder = ServletHolder()
        servletHolder.servlet = WebSocketServlet(connectMethod, disconnectMethod, defaultMethod, topics, sessions)

        handler.addServlet(servletHolder, "")

        server = newServer
    }

    fun start() {
        server?.start()
        server?.join()
    }

    fun stop() {
        server?.stop()
        server = null
    }

    fun startWithoutJoin() {
        server?.start()
    }

    fun addTopic(topic: String, method: LocalWebsocketMethod) {
        when (topic) {
            "\$connect" -> connectMethod = method
            "\$disconnect" -> disconnectMethod = method
            "\$default" -> defaultMethod = method
            else -> topics[topic] = method
        }
    }
}