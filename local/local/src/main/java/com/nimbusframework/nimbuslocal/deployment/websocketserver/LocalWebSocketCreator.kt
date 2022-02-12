package com.nimbusframework.nimbuslocal.deployment.websocketserver

import com.nimbusframework.nimbuslocal.deployment.websocket.LocalWebsocketMethod
import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.server.JettyServerUpgradeRequest
import org.eclipse.jetty.websocket.server.JettyServerUpgradeResponse
import org.eclipse.jetty.websocket.server.JettyWebSocketCreator

class LocalWebSocketCreator(
        private var connectMethod: LocalWebsocketMethod?,
        private var disconnectMethod: LocalWebsocketMethod?,
        private var defaultMethod: LocalWebsocketMethod?,
        private val topics: Map<String, LocalWebsocketMethod>,
        private val sessions: MutableMap<String, Session>
): JettyWebSocketCreator {

    override fun createWebSocket(req: JettyServerUpgradeRequest?, resp: JettyServerUpgradeResponse?): Any? {
        if (req != null) {
            val headers = req.headers.mapValues {
                (_, values) -> values.joinToString(", ")
            }
            val parameters = req.parameterMap.mapValues {
                (_, values) -> values.joinToString(", ")
            }

            return WebSocketAdapter(
                    connectMethod,
                    disconnectMethod,
                    defaultMethod,
                    topics,
                    headers,
                    parameters,
                    sessions
            )
        }
        return null
    }
}
