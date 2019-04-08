package com.nimbusframework.nimbuscore.testing.websocketserver

import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory
import org.eclipse.jetty.websocket.servlet.WebSocketServlet
import com.nimbusframework.nimbuscore.testing.websocket.LocalWebsocketMethod

class WebSocketServlet(
        private var connectMethod: LocalWebsocketMethod?,
        private var disconnectMethod: LocalWebsocketMethod?,
        private val topics: Map<String, LocalWebsocketMethod>,
        private val sessions: MutableMap<String, Session>
) : WebSocketServlet() {

    override fun configure(factory: WebSocketServletFactory) {
        factory.creator = LocalWebSocketCreator(connectMethod, disconnectMethod, topics, sessions)
    }
}