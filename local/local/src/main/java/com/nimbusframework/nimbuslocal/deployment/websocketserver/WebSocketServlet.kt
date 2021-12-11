package com.nimbusframework.nimbuslocal.deployment.websocketserver

import org.eclipse.jetty.websocket.api.Session
import com.nimbusframework.nimbuslocal.deployment.websocket.LocalWebsocketMethod
import org.eclipse.jetty.websocket.server.JettyWebSocketServlet
import org.eclipse.jetty.websocket.server.JettyWebSocketServletFactory

class WebSocketServlet(
        private var connectMethod: LocalWebsocketMethod?,
        private var disconnectMethod: LocalWebsocketMethod?,
        private var defaultMethod: LocalWebsocketMethod?,
        private val topics: Map<String, LocalWebsocketMethod>,
        private val sessions: MutableMap<String, Session>
) : JettyWebSocketServlet() {

    override fun configure(factory: JettyWebSocketServletFactory) {
        factory.setCreator(LocalWebSocketCreator(connectMethod, disconnectMethod, defaultMethod, topics, sessions))
    }
}
